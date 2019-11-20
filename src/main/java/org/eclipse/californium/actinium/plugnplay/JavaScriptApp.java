/*******************************************************************************
 * Copyright (c) 2014 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Martin Lanter - architect and initial implementation
 ******************************************************************************/
package org.eclipse.californium.actinium.plugnplay;

import jdk.internal.dynalink.beans.StaticClass;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.objects.NativeJava;
import org.eclipse.californium.actinium.AppManager;
import org.eclipse.californium.actinium.Utils;
import org.eclipse.californium.actinium.cfg.AppConfig;
import org.eclipse.californium.actinium.cfg.AppType;
import org.eclipse.californium.actinium.cfg.Config;
import org.eclipse.californium.actinium.jscoap.CoapCallback;
import org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants;
import org.eclipse.californium.actinium.jscoap.JavaScriptResource;
import org.eclipse.californium.actinium.jsmodule.JavaScriptModuleObject;
import org.eclipse.californium.actinium.jsmodule.NativeJavaModuleObject;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JavaScriptApp executes apps written in JavaScript using Rhino.
 */
public class JavaScriptApp extends AbstractApp implements JavaScriptCoapConstants {

	private static final AtomicInteger THREAD_ID = new AtomicInteger();

	// The app's configuration
	private AppConfig appcfg; // Note: appcfg can be null if has ben started SimpleAppServer
	
	// The thred that executes the app
	private Thread thread;
	 
	
	// The object "app", that is accessible by JavaScript
	private JavaScriptAccess jsaccess;

	/*
	 * Functions onget, onpost, onput, ondelete can be assigned to from within
	 * JavaScript by e.g. "app.root.onget = ..."
	 */
	public CoapCallback onget, onpost, onput, ondelete; // for JavaScript scripts
	
	// The handler, that makes JavaScript execute requests on "app.root"
	private JSRequestHandler requestHandler;

	private volatile AppContext context;
	private ScriptEngine engine;

	private Map<Object, Object> moduleCache;
	private DynamicClassloader classloader;

	/**
	 * Constructs a new JavaScriptApp with the given appconfig
	 * @param manager
	 * @param appconfig the configuration for this app
	 */
	public JavaScriptApp(final AppManager manager, AppConfig appconfig) {
		super(manager, appconfig);
		moduleCache=new HashMap<>();
		dependencies=new HashSet<>();
		this.appcfg = appconfig;
		this.requestHandler = new JSRequestHandler();
		appconfig.getObservable().addObserver(this);
	}
	
	@Override
	protected synchronized void startImpl() {
		thread = new Thread(this, "JavaScript " + getName() + "-" + THREAD_ID.incrementAndGet());
		thread.start();
	}
	
	@Override
	protected synchronized void restartImpl() {
		if (jsaccess != null) {
			thread.interrupt();
			cleanup();

			for (JavaScriptTimeoutTask task : jsaccess.tasks.values()) {
				task.cancel();
			}
		}
		started = false;
		start();
	}
	
	@Override
	protected synchronized void shutdownImpl() {
		thread.interrupt();
		cleanup(); // call app.onunload in JavaScript
		if (jsaccess != null) {
			for (JavaScriptTimeoutTask task : jsaccess.tasks.values()) {
				task.cancel();
			}
		}
	}

	/**
	 * First, runs the app's JavaScript code. Second executes all incoming
	 * requests for the app and its subresources.
	 */
	@Override
	public void run() {
		try {
			// Load JavaScript code
			String path = appcfg.getProperty(AppConfig.DIR_PATH) + appcfg.getProperty(AppConfig.APP) + "." + AppType.getAppSuffix(appcfg.getProperty(AppConfig.TYPE));
			String code = Utils.readFile(path);

		    // execute code
		    execute(code);

		    // Start receiving requests for this app
		    super.receiveMessages();

		} catch (Exception e) {
        	System.err.println("Exception while executing '" + getName() + "'");
        	e.printStackTrace();
        }
	}

	/**
	 * Executes the given JavaScript code.
	 * @param code the JavaScript code
	 */
	public void execute(String code) {

		if (code==null || code.isEmpty()) return;

		dependencies.clear();
		moduleCache.clear();
		classloader = new DynamicClassloader(Thread.currentThread().getContextClassLoader());
		engine = new NashornScriptEngineFactory().getScriptEngine(classloader);
		try {
			// initialize JavaScrip environment for the app: scope (variables)
			AppContext context = new AppContext();
			this.context = context;
			context.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
			Bindings engineScope = context.getBindings(ScriptContext.ENGINE_SCOPE);

			// Add object "app" to JavaScript
			JavaScriptAccess jsaccess = new JavaScriptAccess();
			engineScope.put("app", jsaccess);
			engineScope.put("require", (IRequire) moduleName -> jsaccess.require(moduleName));
			engineScope.put("_extend", (IExtend) (a, b) -> jsaccess.extend(a, b));
			engineScope.put("_super", (ISuperCall) (a, b,c) -> jsaccess.superCall(a, b, c));
			this.jsaccess = jsaccess;

			String bootstrap = Utils.readFile(JavaScriptApp.class.getResourceAsStream("/bootstrap.js"));
			code =  bootstrap.replaceAll("//.*?\n","\n").replace('\n',' ') + "(function () {" + code + "}).apply({});";

			// Execute code
			if (this.context != null) {
				engine.eval(code, context);
			}
			if (!isStarted()) {
				jsaccess.timer.cancel();
			}
			
		} catch (RuntimeException|ScriptException e) {
			Throwable cause = e.getCause();
			if (cause!=null && cause instanceof InterruptedException) {
				// this was a controlled shutdown, e.g. with app.stop()
				System.out.println("JavaScript app "+getName()+" has been interrupted");
			} else {
				e.printStackTrace();
				if (e instanceof ScriptException) {
					System.err.println("JavaScript error in [" + ((ScriptException) e).getFileName() + "#" + ((ScriptException) e).getLineNumber() + "]: " + e.getMessage());
				}
			}
		}
	}

	@Override
	public long getRunningThreadId() {
		if (thread!=null)
			return thread.getId();
		else return -1;
	}
	
	@Override
	public void handleGET(CoapExchange request) {
		if (this.onget==null) {
			request.respond(ResponseCode.METHOD_NOT_ALLOWED, "GET handler not implemented");
		} else if (!appcfg.getBool(AppConfig.ENABLE_REQUEST_DELIVERY)) {
			request.respond(ResponseCode.FORBIDDEN, "Request delivery has been disabled for this app");
		} else {

			requestHandler.handleGET(request);
		}
	}

	@Override
	public void handlePUT(CoapExchange request) {
		if (this.onput==null) {
			request.respond(ResponseCode.METHOD_NOT_ALLOWED, "PUT handler not implemented");
		} else if (!appcfg.getBool(AppConfig.ENABLE_REQUEST_DELIVERY)) {
			request.respond(ResponseCode.FORBIDDEN, "Request delivery has been disabled for this app");
		} else {
			requestHandler.handlePUT(request);
		}
	}

	@Override
	public void handlePOST(CoapExchange request) {
		if (this.onpost==null) {
			request.respond(ResponseCode.METHOD_NOT_ALLOWED, "POST handler not implemented");
		} else if (!appcfg.getBool(AppConfig.ENABLE_REQUEST_DELIVERY)) {
			request.respond(ResponseCode.FORBIDDEN, "Request delivery has been disabled for this app");
		} else {
			requestHandler.handlePOST(request);
		}
	}

	@Override
	public void handleDELETE(CoapExchange request) {
		if (this.ondelete==null) {
			request.respond(ResponseCode.METHOD_NOT_ALLOWED, "DELETE handler not implemented");
		} else if (!appcfg.getBool(AppConfig.ENABLE_REQUEST_DELIVERY)) {
			request.respond(ResponseCode.FORBIDDEN, "Request delivery has been disabled for this app");
		} else {
			requestHandler.handleDELETE(request);
		}
	}

	/**
	 * Calls the function onunload in the JavaScript script if defined.
	 */
	private void cleanup() {
		if (jsaccess != null) {
			Function<Object, Void> onunload = jsaccess.onunload;
			if (onunload!=null) {
				onunload.apply(null);
			} else {
				System.out.println("No cleanup function in script "+getName()+" defined");
			}
			jsaccess.timer.cancel();
		}
		classloader = null;
		context=null;
		moduleCache.clear();
		System.gc();
	}

	/**
	 * JavaScriptAccess is the class for the object "app", that is accessible
	 * from within JavaScript. It contains "app.root", the resource root and
	 * "app.dump" and "app.error" for printing to the standart output streams
	 * and further functions.
	 */
	public class JavaScriptAccess implements JavaScriptCoapConstants {
		
		public JavaScriptApp root = JavaScriptApp.this; // "app.root"
		
		public Function<Object, Void> onunload = null; // "app.onunload = ..."

		private HashMap<Integer, JavaScriptTimeoutTask> tasks =
			new HashMap<Integer, JavaScriptTimeoutTask>(); // tasks and their id
		// timer to schedule tasks
		private Timer timer = new Timer(getName() + "-timer" + "-" + THREAD_ID.incrementAndGet(), true);
		private int timernr = 1; // increasing task counter

		/**
		 * Prints to the standard output stream
		 * @param args the objects to print
		 */
		public void dump(Object... args) { // "app.dump("hello world");
			if (isOutputAllowed()) {
				String output = convertToOutput(args);
				JavaScriptApp.this.printOutput(output);
			}
		}

		/**
		 * This functions implements the require function defined the CommonJS module format.
		 * http://wiki.commonjs.org/wiki/Modules/1.1.1
		 */
		public Object require(String name) {
			try {
				Object element = moduleCache.get(name);
				if (element == null) {
					String libPath = getManager().getConfig().get(Config.APP_LIBS_PATH) + name;
					File file = new File(libPath + ".js");
					if (file.exists()) {
						element = JavaScriptModuleObject.create(name, engine, context, file);
					} else {
						File propertiesFile = new File(libPath + "/config.cfg");
						Properties properties = new Properties();
						properties.load(new FileInputStream(propertiesFile));
						element = NativeJavaModuleObject.create(engine, context, classloader, propertiesFile, properties);
					}
					moduleCache.put(name, element);
				}
				return element;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ScriptException e) {
				e.printStackTrace();
			}
			return null;
		}

		public Object extend(StaticClass base, final ScriptObjectMirror function) {
			Class<?> cl = base.getRepresentedClass();
			try {
				Set<String> existingMethods = Stream.of(cl.getDeclaredMethods()).map((x) -> x.getName()).collect(Collectors.toSet());

				Set<String> definedMethods = function.keySet();
				List<Object> interfaces = definedMethods.stream().filter(x -> !existingMethods.contains(x)).map(x -> {
					try {
						ScriptObjectMirror fn = ((ScriptObjectMirror) function.get(x));
						if (fn.get("_length") != null)
							return NativeJava.type(null, "gen." + x + "_" + fn.get("_length"));
					} catch (Exception e) { }
					return null;
				}).filter((x) -> x != null).collect(Collectors.toList());

				interfaces.add(0, base);
				interfaces.add(ScriptUtils.unwrap(function));
				return NativeJava.extend(base, interfaces.toArray());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		/**
		 * Prints to the standart error output stream
		 * @param args the objects to print
		 */
		public void error(Object... args) {
			if (isErrorOutputAllowed()) {
				String output = convertToOutput(args);
				JavaScriptApp.this.printErrorOutput(output);
			}
		}
		
		private String convertToOutput(Object[] args) {
			StringBuffer buffer = new StringBuffer();
			if (args.length>0 && args[0]!=null && !"".equals(args[0])) {
				// add "\n"s (newline) from first arg to buffer
				String arg0 = args[0].toString();
				for (int nl=0; arg0.charAt(nl)=='\n'; nl++)
					buffer.append('\n');
			}
			for (int i = 0; i < args.length; i++) {
				if (i == 0) {
					buffer.append(getName()+":\t");
					String str = args[0].toString().replaceFirst("\n*",""); // remove "\n" at beginning
					buffer.append(str.replace("\n", "\n\t"));
				} else {
					buffer.append("\t");
					buffer.append(args[i].toString().replace("\n", "\n\t"));
				}
			}
			return buffer.toString();
		}
		
		/**
		 * Stops the app.
		 * @param args will be ignored
		 */
		public void shutdown(Object... args) {
			JavaScriptApp.this.shutdown();
		}
		
		/**
		 * Restarts the app.
		 * @param args will be ignored
		 */
		public void restart(Object... args) {
			JavaScriptApp.this.restart();
		}
		
		/**
		 * Sleeps for the given amount of milliseconds.
		 * @param millis the time to sleep
		 * @throws InterruptedException if interrupted
		 */
		public void sleep(long millis) throws InterruptedException {
			Thread.sleep(millis);
		}
		
		/**
		 * Returns the current time in milliseconds.
		 * @return the current time in milliseconds
		 */
		public long getTime() {
			return System.currentTimeMillis();
		}

		/**
		 * Returns the current value of the most precise available system timer,
		 * in nanoseconds.
		 * @return the current value of the most precise available system timer,
		 *         in nanoseconds.
		 */
		public long getNanoTime() {
			return System.nanoTime();
		}
		
		/**
		 * Returns the corresponding value to the key, if found or null.
		 * @param key the key
		 * @return the corresponding value to the key, if found or null.
		 */
		public String getProperty(String key) {
			return appcfg.getProperty(key);
		}

		/**
		 * Returns the corresponding value to the key, if found or the given
		 * default value.
		 * 
		 * @param key the key
		 * @param dflt the default if not set
		 * @return the corresponding value to the key, if found or the given
		 *         default value.
		 */
		public String getProperty(String key, String dflt) {
			return appcfg.getProperty(key, dflt);
		}

		/**
		 * Creates a new Task for the specified function and the specified
		 * arguments and adds it to the timer.
		 * 
		 * @param function the function to call
		 * @param millis the time until the function is called in milliseconds
		 * @param args the arguments for the function
		 * @return the id of the created task
		 */
		public synchronized int setTimeout(Function<Object, Void> function, long millis, Object... args) {
			if (function==null) throw new NullPointerException("app.setTimeout expects function not null");
			int nr = timernr++;
			JavaScriptTimeoutTask task = new JavaScriptTimeoutTask(function, args);
			tasks.put(nr, task);
			timer.schedule(task, millis);
			return nr;
		}
		
		/**
		 * Cancels the specified task
		 * 
		 * @param id the task's id
		 */
		public synchronized void clearTimeout(int id) {
			JavaScriptTimeoutTask task = tasks.get(id);
			if (task!=null) {
				task.cancel();
			}
		}

		/**
		 * Creates a new Task for the specified function and the specified
		 * arguments and adds it to the timer. The timer executes it
		 * subsequentially after the specified amount of time.
		 * 
		 * @param function the function to call
		 * @param millis the time until the function is called in milliseconds
		 * @param args the arguments for the function
		 * @return the id of the created task
		 */
		public synchronized int setInterval(Function<Object, Void> function, long millis, Object... args) {
			if (function==null) throw new NullPointerException("app.setInterval expects function not null");
			int nr = timernr++;
			JavaScriptTimeoutTask task = new JavaScriptTimeoutTask(function, args);
			tasks.put(nr, task);
			timer.scheduleAtFixedRate(task, millis, millis);
			return nr;
		}
		
		/**
		 * Cancels the specified task
		 * 
		 * @param id the task's id
		 */
		public synchronized void clearInterval(int id) {
			clearTimeout(id);
		}

		public Object superCall(Object a, String method_name, Object[] arguments) {
			try {
				Object val = a.getClass().getMethod("super$" + method_name).invoke(a,arguments);
				return val;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/**
	 * When app.setTimeout is called a new task for a specified function is
	 * created and added to the timer. After the specified milliseconds have
	 * passed this task is executed and adds the function to the worker
	 * queue of the app.The app's thread executes this runnable and calls
	 * the specified function.
	 */
	// This class must be a inner class so that deliveRunnable() can be called.
	// Ugly but neccessary.
	private class JavaScriptTimeoutTask extends TimerTask {
		
		private Function<Object, Void> function; // the function
		
		public JavaScriptTimeoutTask(Function<Object, Void> function, Object[] args) {
			this.function = function;
		}

		/**
		 * This function is called from the timer after the specified amount
		 * of time has passed. It then adds a Runnable to the apps worker
		 * queue. The app's thread executes this runnable and calls the
		 * specified function.
		 */
		@Override
		public void run() {
			// add function to working queue
			deliveRunnable(new FunctionExecuter());
		}

		/**
		 * FunctionExecuter only wraps the function call. Therefore it can
		 * be added to the app's worker queue.
		 */
		private class FunctionExecuter implements Runnable {
			public void run() {
				function.apply(null);
			}
		}
	}

	/*
	 * Gives this resource the same abilities as JavaScriptResource that the
	 * scripts use internally without actually inheriting from it.
	 */
	private class JSRequestHandler extends JavaScriptResource {
		
		@Override
		public CoapCallback getOnget() {
			return JavaScriptApp.this.onget;
		}

		@Override
		public CoapCallback getOnpost() {
			return JavaScriptApp.this.onpost;
		}

		@Override
		public CoapCallback getOnput() {
			return JavaScriptApp.this.onput;
		}

		@Override
		public CoapCallback getOndelete() {
			return JavaScriptApp.this.ondelete;
		}
		
		// Return the resource for which the request is meant
		@Override
		public Object getThis() {
			return jsaccess.root;
		}
	}

	@FunctionalInterface
	public interface IRequire {
		Object call(String name);
	}

	@FunctionalInterface
	public interface IExtend {
		Object call(StaticClass a, ScriptObjectMirror b);
	}
	@FunctionalInterface
	public interface ISuperCall {
		Object call(Object target, String method, Object[] args);
	}
}

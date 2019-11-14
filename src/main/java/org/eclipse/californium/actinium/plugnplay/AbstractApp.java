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

import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.californium.actinium.AppManager;
import org.eclipse.californium.actinium.cfg.AbstractConfig.ConfigChangeSet;
import org.eclipse.californium.actinium.cfg.AppConfig;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * AbstractApp is the parent class for all sorts of apps and a CoAP resource. So
 * far only JavaScrip apps are implemented. CoAP requests sent to such an app is
 * forwarded to the app and handled by the app's program code. If an app doesn't
 * define a handler for a GET, POST, PUT or DELETE request the default response
 * with code RESP_METHOD_NOT_ALLOWED is responded.
 * <p>
 * AbstractApp also implements PlugAndPlayable, which are the methods start(),
 * shutdown(), restart() and getName(). It also implements Observer, so that it
 * is able to observe its AppConfig and gets notified, when the AppConfig
 * changes. E.g. the property "running" might change, triggering AbstractApp to
 * start, stop or restart itself.
 */
public abstract class AbstractApp extends CoapResource implements PlugAndPlayable, Observer {

	// Logging /////////////////////////////////////////////////////////////////////
		
	protected static final Logger LOG = Logger.getLogger(AbstractApp.class.getName());

	private final AppManager manager;
	// properties of this app
	private final AppConfig appcfg;
	
	// only for internal use (and subclasses).
	protected volatile boolean started;
	
	private long startTimestamp; // timestamp, when app has started
	private long stopTimestamp; // timestamp, when app has stopped (shutdown)
	
	private boolean allowOutput; // true, if this app is allowed to print to standard output
	private boolean allowErrorOutput; // true, if this app is allowed to print to standard error output
	
	// Recevier for all requests, which then get executed one after another by the app's thread
	private WorkQueue requestReceiver;
	public Set<String> dependencies;

	/**
	 * Constructs a new AbstractApp with the specified properties. If the
	 * AppConfig defines a special reousece title or type, they will be used.
	 *
	 * @param manager
	 * @param appcfg the properties for this app instance.
	 */
	public AbstractApp(final AppManager manager, AppConfig appcfg) {
		super(appcfg.getName().toLowerCase());
		this.manager = manager;
		this.appcfg = appcfg;
		this.allowOutput = appcfg.getBool(AppConfig.ALLOW_OUTPUT);
		this.allowErrorOutput = appcfg.getBool(AppConfig.ALLOW_ERROR_OUTPUT);
		
		String resourceTitle = appcfg.getProperty(AppConfig.RESOURCE_TITLE);
		if (resourceTitle!=null)
			getAttributes().setTitle(resourceTitle);
		
		String resourceType = appcfg.getProperty(AppConfig.RESOURCE_TYPE);
		if (resourceType!=null)
			getAttributes().addResourceType(resourceType);

		this.requestReceiver = new WorkQueue(appcfg.getName()+"-ReceiverThread");
	}

	/**
	 * Returns the AppConfig of this app.
	 * @return the AppConfig of this app.
	 */
	public AppConfig getConfig() {
		return appcfg;
	}


	
	public long getStartTimestamp() {
		return startTimestamp;
	}
	
	public long getStopTimestamp() {
		return stopTimestamp;
	}

	@Override
	public String getName() {
		return appcfg.getName();
	}
	
	// Make changed public (e.g. for JS)
	@Override
	public void changed() {
		super.changed();
	}

	/**
	 * Checks for the property "running" and starts, stops or restarts the app
	 * accordingly.
	 */
	@Override
	public synchronized void update(Observable o, Object arg) {
		if (! (arg instanceof AppConfig.ConfigChangeSet))
			return;
		ConfigChangeSet set = (ConfigChangeSet) arg;
		if (set.contains(AppConfig.RUNNING)) {
			String running = appcfg.getProperty(AppConfig.RUNNING);
			if (running.equals(AppConfig.START) && !started) {
				start();
			} else if (running.equals(AppConfig.RESTART)) {
				restart();
			} else if (running.equals(AppConfig.STOP) && started) {
				shutdown();
			}
		}
		if (set.contains(AppConfig.RESOURCE_TITLE)) {
			getAttributes().setTitle(appcfg.getProperty(AppConfig.RESOURCE_TITLE));
		}
		if (set.contains(AppConfig.RESOURCE_TYPE)) {
			getAttributes().addResourceType(appcfg.getProperty(AppConfig.RESOURCE_TYPE));
		}
		if (set.contains(AppConfig.ALLOW_OUTPUT)) {
			allowOutput = appcfg.getBool(AppConfig.ALLOW_OUTPUT);
		}
		if (set.contains(AppConfig.ALLOW_ERROR_OUTPUT)) {
			allowErrorOutput = appcfg.getBool(AppConfig.ALLOW_ERROR_OUTPUT);
		}
	}

	/**
	 * Starts the app. This method calls startImpl which must be implemented by
	 * subclasses and starts the actual execution of the app's code.
	 */
	@Override
	public synchronized void start() {
		if (started) {
			System.err.println("App "+getName()+" cannot be started more then once at a time");
		} else {
			System.out.println("App "+getName()+" starts in new thread");
			stopTimestamp = 0;
			startTimestamp = System.currentTimeMillis();
			started = true;
			if (requestReceiver != null && !requestReceiver.isRunning()) {
				requestReceiver = new WorkQueue(appcfg.getName()+"-ReceiverThread");
			}
			appcfg.setPropertyAndNotify(
					AppConfig.RUNNING, AppConfig.START);
			
			startImpl();
		}
	}

	/**
	 * Shuts down the app and removes all subresources. This method calls
	 * shutdownImpld which must be implemented by subclasses and stops the
	 * actual execution of the app's code.
	 */
	@Override
	public synchronized void shutdown() {
		if (!started) {
			System.err.println("App "+getName()+" is already shutdown");
		} else {
			System.out.println("App "+getName()+" shutdown");
			started = false;
			requestReceiver.stop();
			removeSubresources();
			shutdownImpl();
			
			stopTimestamp = System.currentTimeMillis();
			appcfg.setPropertyAndNotify(
					AppConfig.RUNNING, AppConfig.STOP);
		}
	}
	
	/**
	 * Restarts the app and removes all subresources. This method calls
	 * restartImpl which must be implemented by subclasses and restarts the
	 * actual execution of the app's code.
	 */
	@Override
	public synchronized void restart() {
		System.out.println("App "+getName()+" restart");

		requestReceiver.stop();
		removeSubresources();
		startTimestamp = System.currentTimeMillis();
		restartImpl();
		stopTimestamp = 0;
	}
	
	/**
	 * Removes all subresources
	 */
	private void removeSubresources() {
		for (Resource res:getChildren())
			delete(res);
	}
	
	/**
	 * Delivers the specified request to the specified resource
	 * @param request the request
	 * @param resource the target resource
	 */
	public void deliverRequestToSubResource(CoapExchange request, CoapResource resource) {
		requestReceiver.deliver(request, resource);
	}
	
	/**
	 * Delivers the specified Runnable to the app's worker queue
	 * @param runnable the runnable
	 */
	public void deliveRunnable(Runnable runnable) {
		requestReceiver.deliver(runnable);
	}
	
	/**
	 * Returns true, if this app is allowed to print to the output stream.
	 * @return true, if this app is allowed to print to the output stream.
	 */
	public boolean isOutputAllowed() {
		return allowOutput;
	}
	
	/**
	 * Retunrs true, if this app is allowed to print to the error stream.
	 * @return true, if this app is allowed to print to the error stream.
	 */
	public boolean isErrorOutputAllowed() {
		return allowErrorOutput;
	}
	
	/**
	 * Prints to the output stream
	 * @param output the text
	 */
	protected void printOutput(String output) {
		if (isOutputAllowed())
			System.out.println(output);
	}
	
	/**
	 * Prints to the error stream
	 * @param output the text
	 */
	protected void printErrorOutput(String output) {
		if (isErrorOutputAllowed())
			System.err.println(output);
	}
	
	/**
	 * Retunrs true, if this app has started
	 * @return true, if this app has started
	 */
	protected boolean isStarted() {
		return started;
	}

	/**
	 * Start receiving messages. The thread that executes the app calls this
	 * method and then handles all requests, that arrive for the app or its
	 * subresources.
	 */
	protected void receiveMessages() {
		requestReceiver.execute();
	}
	
	/**
	 * Starts this app. Should create a new Thread for that.
	 */
	protected abstract void startImpl();
	
	/**
	 * Stops this app (the executing thread)
	 */
	protected abstract void shutdownImpl();
	
	/**
	 * Restarts this app.
	 */
	protected abstract void restartImpl();

	/**
	 * Returns the id of the thread, that is executing the app or -1.
	 * 
	 * @return the executing thread or -1
	 */
	public abstract long getRunningThreadId();

	public AppManager getManager() {
		return manager;
	}
}

/*******************************************************************************
 * Copyright (c) 2014 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Martin Lanter - architect and initial implementation
 ******************************************************************************/
package org.eclipse.californium.actinium.install;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.Scanner;

import org.eclipse.californium.actinium.AppManager;
import org.eclipse.californium.actinium.cfg.AppConfig;
import org.eclipse.californium.actinium.cfg.Config;

import ch.ethz.inf.vs.californium.coap.CodeRegistry;
import ch.ethz.inf.vs.californium.coap.DELETERequest;
import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.POSTRequest;
import ch.ethz.inf.vs.californium.coap.PUTRequest;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.endpoint.LocalResource;

/**
 * InstalledAppResource represents an app's code, whichs is stored to the disk.
 * To create a running instance of this app, a POST request to the
 * InstalledAppResource is required.
 * <p>
 * On a GET request, it responses with the content of this app. On a POST
 * request, it creates a new instance of this app and adds it to AppResource. On
 * a PUT request, InstalledAppResource updates the app's content. On a DELETE
 * request, the app's file, all its instances and this resource are removed.
 */
public class InstalledAppResource extends LocalResource {
	
	// the name of this app
	private String name;
	
	// the AppManager that manages the instances of all apps
	private AppManager manager;

	/**
	 * Constructs a new InstalledAppResource with the given config and name. It
	 * is assumed, that the related code to this name is already stored on the
	 * disk. InstallResource calls this constructur during startup to gather the
	 * installed resources.
	 * 
	 * @param config - The config of the server
	 * @param name - The name of the installed app and this resource
	 */
	public InstalledAppResource(String name, AppManager manager) {
		super(name);
		this.name = name;
		this.manager = manager;
		
		isObservable(true);
	}

	/**
	 * Constructs a new InstalledAppResource with the given config, name and
	 * code. The code will be stored to the disk.
	 * 
	 * @param config - The config of the server
	 * @param name - The name of the installed app and this resource
	 * @param code - The code of the installed app
	 */
	public InstalledAppResource(String name, String code, AppManager manager) {
		this(name, manager);
		storeApp(code);
	}

	/**
	 * Responds the content of the app.
	 */
	@Override
	public void performGET(GETRequest request) {
		try {
			File file = new File(getInstalledPath());
			Scanner scanner = new Scanner(file).useDelimiter("\\Z");
		    String content = scanner.next();  
		    scanner.close();
			request.respond(CodeRegistry.RESP_CONTENT, content);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			request.respond(CodeRegistry.RESP_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Updates the app. On an update, all instances of this app will be
	 * restarted.
	 */
	@Override
	public void performPUT(PUTRequest request) {
		try {
			
			// update
			String code = request.getPayloadString();
			storeApp(code);
			
			// restart all instances
			manager.restartApps(name);
			
			changed();
			request.respond(CodeRegistry.RESP_CHANGED);
			
		} catch (Exception e) { // should not happen
			e.printStackTrace();
			request.respond(CodeRegistry.RESP_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Creates a new instance of this app and adds it to the AppResource.
	 * <p>
	 * First, it creates a new AppConfig, constructed out of the request's
	 * content. This content is supposed to be of the form of a Java properties
	 * file (e.g. "key = value"). Any property can be specified (like
	 * start_on_startup, type, name etc.). However, at least the property name
	 * must be specified. Thus the content "name = myapp" is already enought to
	 * create an instance with the name myapp.
	 * <p>
	 * Second, the AppManager installs the newly created app in AppResource. If
	 * the specified name is already in use or any other inconsistency occurs,
	 * it throws an IllegalArgumentException. If everything goes well, it adds
	 * the AppConfig to the AppConfigsResource and if the app starts up the
	 * AppResource adds the app's resource to the RunningResource.
	 * <p>
	 * Finally, if the installation process was successful, the path to the
	 * newly created app is stored in the lcoation header of the response and
	 * responded. If the installation process was not successful, however, the
	 * response has code RESP_BAD_REQUEST.
	 */
	@Override
	public void performPOST(POSTRequest request) {
		try {
			String payload = request.getPayloadString();
			
			// create a new AppConfig
			AppConfig appcfg = convertToAppConfig(payload);
			appcfg.setProperty(AppConfig.APP, name);
			
			String newpath = manager.instantiateApp(appcfg);

			// inform client about the location of the new resource
			Response response = new Response(CodeRegistry.RESP_CREATED);
			response.setPayload("Application "+name+" successfully installed to "+newpath);
			response.setLocationPath(newpath);
			
			request.respond(response);
			
		} catch (IllegalArgumentException e) { // given query invalid
			System.err.println(e.getMessage());
			request.respond(CodeRegistry.RESP_BAD_REQUEST, e.getMessage()); // RESP_PRECONDITION_FAILED?
		
		} catch (RuntimeException e) { // some error while processing (e.g. IO)
			e.printStackTrace();
			System.err.println(e.getMessage());
			request.respond(CodeRegistry.RESP_BAD_REQUEST, e.getMessage()); // RESP_PRECONDITION_FAILED?
			
		} catch (Exception e) { // should not happen
			e.printStackTrace();
			request.respond(CodeRegistry.RESP_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Deletes this app and all instances of it from the server. The AppManager
	 * stops the execution of all instances of this app and removes all config
	 * files from their parent resources and from the disk.
	 */
	@Override
	public void performDELETE(DELETERequest request) {
		try {
			manager.deleteApps(name); // throws IOException if not successful (e.g. no write-access to config file)
			deleteApp(); // throws IOException if not successful (e.g. no write-access to javascript file)
			remove();
			request.respond(CodeRegistry.RESP_DELETED);
		} catch (IOException e) {
			e.printStackTrace();
			request.respond(CodeRegistry.RESP_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Converts the specified payload to a new AppConfig. The payload must be of
	 * the form of a Java properties file.
	 * 
	 * @param payload the properties of the new AppConfig
	 * @return the new AppConfig
	 */
	private AppConfig convertToAppConfig(String payload) {
		Properties p = new Properties();
		try {
			StringReader reader = new StringReader(payload);
			p.load(reader);
			
			System.out.println("New app config:");
			for (Object key:p.keySet()) System.out.println("	"+key+" = >"+p.get(key)+"<");
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Configuration was not able to be parsed", e);
		}
		
		AppConfig appcfg = new AppConfig(p);
		return appcfg;
	}

	/**
	 * Store the given code to disk. This happens, when a new app is sent to the
	 * InstallResource or when an app is updated with new code.
	 * 
	 * @param code the code to store to the disk.
	 */
	private void storeApp(String code) {
		FileWriter appfile = null; 
		try {
			String apppath = getInstalledPath();
			appfile = new FileWriter(apppath);
			appfile.write(code);
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Internal error while trying to store app to disk. IOException: "+e.getMessage(),e);
		} finally {
			try {
				if (appfile!=null)
					appfile.close();
			} catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	/**
	 * Deletes the file with the code of this app.
	 * @throws IOException if file does not exist or is not deletable.
	 */
	private void deleteApp() throws IOException {
		String apppath = getInstalledPath();
		File file = new File(apppath);
		if (!file.exists())
			throw new IOException("The file "+apppath+" of app "+name+" doesn't exist on the disk");
		
		if (!file.canWrite())
			throw new IOException("The file "+apppath+" of app "+name+" is not writable/deletable");
		
		System.out.println("Delete app "+apppath);
		boolean success = file.delete();
		
		if (!success)
			throw new IOException("The file "+apppath+" of app "+name+" couldn't be deleted. Make sure, no other process is accessing it");
	}
	
	/**
	 * Returns the path to the file with the code of this app.
	 * @return the path to the file with the code of this app.
	 */
	private String getInstalledPath() {
		return manager.getConfig().getProperty(Config.APP_PATH)+name+manager.getConfig().getProperty(Config.JAVASCRIPT_SUFFIX);
	}
}

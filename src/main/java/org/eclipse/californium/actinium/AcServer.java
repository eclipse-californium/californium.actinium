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
package org.eclipse.californium.actinium;

import java.net.SocketException;

import org.eclipse.californium.actinium.cfg.Config;
import org.eclipse.californium.actinium.install.InstallResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * Actinium (Ac) App-server for Californium
 * <p>
 * An Actinium app-server creates a structure of CoAP resources to run arbitrary
 * JavaScript apps on it. Most Notably an InstallResource to install new apps
 * and an AppResource which holds the instances of those apps.
 * <p>
 * An app or its subresources must not block the receiver thread of Californium.
 * Therefore every app has its own thread for handling requests. If a requests
 * has an app or a subresource of an app as target the server passes the request
 * to this app, whose receiver thread then will handle the request.
 */
public class AcServer extends CoapServer {

	// appserver's configuration
	private Config config;

	// AppManager to figure out to which apps a request belongs
	private AppManager manager;
	
	// resource that holds the stats for all app instances
	private StatsResource stats;
	
	/**
	 * Constructs a new Actinium app-server with the specified config.
	 * @param config the app server's config.
	 * @throws SocketException if the Socket is blocked.
	 */
	public AcServer(Config config) throws SocketException {
		
		//Log.setLevel(Level.ALL);
		this.config = config;
		
		this.manager = new AppManager(config);
		
		AppResource appres = new AppResource(manager);
		InstallResource insres = new InstallResource(manager);

		this.add(appres);
		this.add(insres);

		this.add(
				config.createConfigResource(config.getProperty(Config.CONFIG_RESOURCE_ID)));
		
		this.stats = new StatsResource(config, manager);
		this.add(stats);
		appres.startApps();
		
		//this.addResource(new TODOResource());
	}

//	/**
//	 * Catch any exceptions throws inside handleRequest. Otherwise an exception
//	 * will halt the ReiceiverThread and stop Californium.
//	 */
//	@Override
//	public void handleRequest(CoapExchange request) {
//		try {
//			// record message
//			Resource resource = getResource( request.getOptions().getURIPathString() );
//			if (resource!=null)
//				stats.record(request, resource);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			// deliver message to receiver
//			deliverRequest(request);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	// from super.handleRequest with special treat for subresources of apps
//	private void deliverRequest(Request request) {
//		if (request != null) {
//
//			// lookup resource
//			CoapResource resource = getResource( request.getUriPath() );
//
//			// check if resource available
//			if (resource != null) {
//				
//				request.setResource(resource);
//				
//				if (resource instanceof JavaScriptApp) {
//					
//					JavaScriptApp appRes = (JavaScriptApp) resource;
//					
//					String appname = appRes.getName();
//					if (appname!=null) { // request for a subresource of an app
//						// invoke request handler of the app the resource belongs to
//						/*
//						 * An app or its subresources must not block the receiver
//						 * thread. Therefore every app has its own thread for
//						 * handling requests.
//						 */
//						AbstractApp app = manager.getApp(appname);
//						app.deliverRequestToSubResource(request, resource);
//					}
//					
//
//				} else if (resource instanceof JavaScriptResource) {
//					
//					String appname = getAppName(resource);
//					
//					if (appname!=null) { // request for a subresource of an app
//						// invoke request handler of the app the resource belongs to
//						/*
//						 * An app or its subresources must not block the receiver
//						 * thread. Therefore every app has its own thread for
//						 * handling requests.
//						 */
//						AbstractApp app = manager.getApp(appname);
//						app.deliverRequestToSubResource(request, resource);
//					}
//					
//				} else {
//					// invoke request handler of the resource
//					request.dispatch(resource);
//				}
//
//			} else {
//				// resource does not exist
//				System.out.printf("[%s] Resource not found: '%s'\n", getClass().getName(), request.getUriPath());
//
//				request.respond(CodeRegistry.RESP_NOT_FOUND);
//			}
//		}
//	}
	
	/**
	 * Returns the name of the app instance to which the specified resource
	 * belongs to or null if it corresponds to no app instance.
	 * 
	 * @param res the resource.
	 * @return the app instance it belongs to or null.
	 */
	private String getAppName(Resource res) {
		// path in this form: /apps/running/appname/...
		String path = res.getPath();
		String[] parts = path.split("/"); // parts[0] is ""
		
		String idapps = config.getProperty(Config.APPS_RESOURCE_ID);
		String idrun = config.getProperty(Config.RUNNING_RESOURCE_ID);
		
		if (parts.length>3 && parts[1].equals(idapps) && parts[2].equals(idrun)) {
			return parts[3];
		} else {
			return null;
		}
	}
	
	/**
	 * Setups a new config and a new app server.
	 * @param args no arguments required
	 */
	public static void main(String[] args) {
		try {
			
//			Log.setLevel(Level.WARNING);
//			Log.init();
			
			Config config = new Config();
			AcServer server = new AcServer(config);
			server.start();
			
			System.out.println("Actinium (Ac) App-server listening on port "+server.getEndpoints().get(0).getAddress().getPort());
			
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
}

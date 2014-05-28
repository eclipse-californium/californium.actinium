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
import java.util.List;

import org.eclipse.californium.actinium.AppManager;
import org.eclipse.californium.actinium.cfg.Config;

import ch.ethz.inf.vs.californium.coap.CodeRegistry;
import ch.ethz.inf.vs.californium.coap.DELETERequest;
import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.Option;
import ch.ethz.inf.vs.californium.coap.OptionNumberRegistry;
import ch.ethz.inf.vs.californium.coap.POSTRequest;
import ch.ethz.inf.vs.californium.coap.PUTRequest;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.endpoint.LocalResource;

/**
 * The InstallResource is the parent resource of all available apps which an
 * instance can be created of. To install a new app, send a POST request with
 * the desired name in the query and the program code as content.
 * InstallResource then checks, if the specified name is valid (e.g. not already
 * in use) and if it is fine, creates a new subresource of type
 * InstalledAppResource and adds it as subresource. This also stores the app's
 * content to disk.
 * <p>
 * Note, It does not create an instance of this app already, only s POST to such
 * a InstalledAppResource will create an instance of the app.
 * <p>
 * Note, that neither InstallResource nor InstalledAppResource cares, what kind
 * of app (JavaScript, binary, etc.) you are installing. InstallResource only
 * creates a new InstalledAppResource with whatever content it receives.
 */
public class InstallResource extends LocalResource {
	
	// The reference to the AppManager is only needet to give it to InstalledAppResource
	private AppManager manager;

	/**
	 * Constructs a new InstallResource with the specified config and the
	 * specified AppManager.
	 * 
	 * @param config the config of the app server
	 * @param manager the manager for all apps
	 */
	public InstallResource(AppManager manager) {
		super(manager.getConfig().getProperty(Config.INSTALL_RESOURCE_ID));
		
		this.manager = manager;
		
		String[] installed = getInstalledAppsName();
		for (String inst:installed) {
			InstalledAppResource res = new InstalledAppResource(inst, manager);
			addInstalledAppResource(res);
		}
		
		System.out.println("Installation resource is ready");
	}
	
	/**
	 * Adds a InstalledAppResource. Adds is to its subresources.
	 * @param iar the InstalledAppResource
	 */
	private void addInstalledAppResource(InstalledAppResource iar) {
		add(iar);
	}
	
	/**
	 * Responds with a list of all installed apps.
	 */
	@Override
	public void performGET(GETRequest request) {
		StringBuffer buffer = new StringBuffer();
		
		// list all apps
		String[] apps = getInstalledAppsName();
		for (String app:apps) {
			buffer.append(app+"\n");
		}
		
		request.respond(CodeRegistry.RESP_CONTENT, buffer.toString());
	}

	/**
	 * A POST request installs a new app. It retrieves the app's name from the
	 * query. Thus the URL of the POST requests must be of the form
	 * [host]/install?[appname]. InstallResource takes the request's content as
	 * code.
	 * <p>
	 * If the specified name for the app is valid, i.e. not already in use and
	 * not empty, InstallResource creates a new file on the disk and stores the
	 * request's content to it. Otherwise it responds with code
	 * RESP_BAD_REQUEST.
	 * <p>
	 * InstallResource then creates a new subresource of type
	 * InstalledAppResource, which represents the insalled app. It also stores
	 * the subresource's location in the location headder of the response
	 */
	@Override
	public void performPOST(POSTRequest request) {
		System.out.println("Installer receivesd data");
		try {
			// Figure out, whether payload is String or byte[] and install
			String payload = request.getPayloadString();
			String query = extractQuery(request);	
			
			// Throws IllegaArgumentException if request is not legal
			String newpath = installAppFromString(payload, query);

			Response response = new Response(CodeRegistry.RESP_CREATED);
			response.setPayload("Application "+query+" successfully installed to "+newpath);

			// Inform client about the location of the new resource
			response.setLocationPath(newpath);
			
			request.respond(response);
		
		} catch (IllegalArgumentException e) { // given query invalid
			System.err.println(e.getMessage());
			request.respond(CodeRegistry.RESP_BAD_REQUEST, e.getMessage()); // RESP_PRECONDITION_FAILED?
		
		} catch (RuntimeException e) { // some error while processing (e.g. IO)
			e.printStackTrace();
			request.respond(CodeRegistry.RESP_BAD_REQUEST, e.getMessage()); // RESP_PRECONDITION_FAILED?
			
		} catch (Exception e) { // should not happen
			e.printStackTrace();
			request.respond(CodeRegistry.RESP_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	@Override
	public void performPUT(PUTRequest request) {
		request.respond(CodeRegistry.RESP_METHOD_NOT_ALLOWED);
	}

	@Override
	public void performDELETE(DELETERequest request) {
		request.respond(CodeRegistry.RESP_METHOD_NOT_ALLOWED);
	}
	
	/**
	 * Extractes the query as string from the specified request.
	 * @param request the request
	 * @return the query
	 */
	private String extractQuery(Request request) {
		List<Option> opts = request.getOptions(OptionNumberRegistry.URI_QUERY);
		
		if (opts==null || opts.size()==0) {
			throw new IllegalArgumentException("No query in uri with the name of the app has been found (use e.g. uri= /install?my_app_name)");
		} else {
			return opts.get(0).getStringValue();
		}
	}

	/**
	 * Checks, if the specified name is null or already in use. If so, throws an
	 * IllegalArgumentException. If the name is fine, it creates a new
	 * subresource of type InstalledAppResource with the specified name as
	 * identifier and the specified payload and adds it as subresource.
	 * 
	 * @param payload the payload of the app
	 * @param name the name of the app
	 * @return the path to the newly created resource
	 */
	private String installAppFromString(String payload, String name) {
		System.out.println("install "+name);
		
		if (name==null)
			throw new IllegalArgumentException("The given name is null. Please specify a valid name in the uri query");
		if (!isUnreservedName(name))
			throw new IllegalArgumentException("The given name "+name+" is already in use. " +
					"Choose another name or update the current app with a PUT request");

		// we have a valid name, store program to disk
		InstalledAppResource res = new InstalledAppResource(name, payload, manager);
		addInstalledAppResource(res);
		
		return res.getPath();
	}
	
	/**
	 * Checks, if the specified name is already in use for another app.
	 * @param name the name of the app
	 * @return true, if another app already uses this name
	 */
	private boolean isUnreservedName(String name) {
		String filename = getAppPath(name);
		return !new File(filename).exists();
	}
	
	/**
	 * Returns the path to the app with the specified name.
	 * @param name the name of the app
	 * @return the path to the app with this name
	 */
	private String getAppPath(String name) {
		String path = manager.getConfig().getProperty(Config.APP_PATH);
		return path + name + manager.getConfig().getProperty(Config.JAVASCRIPT_SUFFIX);
	}

	/**
	 * Returns an array of the names of all installed apps. This is used in the
	 * constructor to find the preinstalled apps on the disk.
	 * 
	 * @return an array with the names of all installed apps
	 */
	private String[] getInstalledAppsName() {
		String path = manager.getConfig().getProperty(Config.APP_PATH);
		String[] files = new File(path).list();
		String[] ret = new String[files.length];
		for (int i=0;i<files.length;i++) {
			int last = files[i].lastIndexOf('.');
			if (last<0) ret[i] = files[i];
			else ret[i] = files[i].substring(0,last);
		}
		return ret;
	}
}

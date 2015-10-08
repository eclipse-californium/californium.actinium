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
package org.eclipse.californium.actinium.cfg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

/**
 * An AppConfig contains the properties for an instance of an app. It contains
 * informations like what code is going to be executed, what type the code is
 * of, the name of the app instance and more.
 * <p>
 * The property "running" represents the information in what running state the
 * app is supposed to be. If it is "start", the app is supposed to stert. If it
 * is "stop", the app is supposed to stop. If it is "restart" the app is
 * supposed to stop and start again and put its running state to "start".
 * <p>
 * The property "availibility" represents, whether the app is available to
 * start, stop or restart at all. An app only gets unavailable, if it is
 * deleted, either to a direct deletion of its AppConfig or to a deletion of the
 * app it is an instance of, which alos leads to a deletion of the AppConfig. If
 * the AppConfig gets deleted and the property gets set to "unavailable" it
 * cannot be resetted and must be removed and later reinstantiated again.
 */
public class AppConfig extends AbstractConfig {

	private static final long serialVersionUID = -318048901712176295L;

	public static final String UNNAMED = "unnamed"; // if an app has no name (should not happen)

	// keys for AppConfig
	public static final String APP = "app"; // the app (code) that is to be executed
	public static final String DIR_PATH = "dir_path"; // the path to the directory with apps
	
	public static final String TYPE = "type"; // the type of the app. JavaScript is the only implemented so far
	public static final String NAME = "name"; // tha name of the instance of the app
	public static final String RESOURCE_TITLE = "resource_title"; // the title the app's resource will have (CoAP related)
	public static final String RESOURCE_TYPE = "resource_type"; // the type the app's resource will have (CoAP related)
	
	public static final String START_ON_STARTUP = "start_on_startup"; // true, if this app is supposed to start on startup, false otherwise
	
	public static final String ALLOW_OUTPUT = "allow_output";
	public static final String ALLOW_ERROR_OUTPUT = "allow_error_output";
	
	public static final String ENABLE_REQUEST_DELIVERY = "enable_request_delivery";
	
	public static final String RUNNING = "running"; // what state is desired. Allowed are {start, stop, restart}
	public static final String START = "start";
	public static final String STOP = "stop";
	public static final String RESTART = "restart";
	
	public static final String AVAILABILITY = "availability"; // whether this app is available at all (or deleted)
	public static final String AVAILABLE = "available"; // app is available, can be started
	public static final String UNABAILABLE = "unavailable"; // app has been deleted
	
	// the properties that cannot be changed through a POST request
	private HashSet<String> unmodifiable = new HashSet<String>(
			Arrays.asList(NAME, DIR_PATH, AVAILABILITY));

	/**
	 * Constructs a new AppConfig with default properties. properties. This is
	 * used, for apps running on the SimpleAppServer.
	 * 
	 */
	public AppConfig() {
		init();
	}
	
	/**
	 * Constructs a new AppConfig and populates it with the specified
	 * properties. This is used, when a new app instance is created.
	 * 
	 * @param probs the pre defined properties
	 */
	public AppConfig(Properties probs) {
		this();
		putAll(probs);
		makeAllKeysToLowercase();
	}

	/**
	 * Constructs a new AppConfig with the properties, specified in the given
	 * file. This is used, when the app server starts up and loads all app
	 * instances from the disk.
	 * 
	 * @param file the file with the properties on the disk.
	 * @throws IOException if a problem with the given file occurs.
	 */
	public AppConfig(File file) throws IOException {
		super(file.getAbsolutePath());
		init();
		FileInputStream fis = new FileInputStream(file);
		try {
			load(fis);
		} finally {
			fis.close();
		}
	}
	
	public String getName() {
		String name = getProperty(NAME);
		if (name==null) return UNNAMED;
		else return name;
	}
	
	/**
	 * Returns true, if the specified key is modifiable by a POST request.
	 */
	@Override
	public boolean isModifiable(String key) {
		if (unmodifiable.contains(key))
			return false;
		else return true;
	}
	
	@Override
	public void handlePOST(CoapExchange request) {
		String payload = request.getRequestText().trim();
		if (payload.equalsIgnoreCase(START) ||
				payload.equalsIgnoreCase(STOP) ||
				payload.equalsIgnoreCase(RESTART)) {
			put(RUNNING, payload);
			fireNotification(RUNNING);
			request.respond(ResponseCode.CHANGED, "put running = "+payload);
		} else {
			super.handlePOST(request);
		}
	}
	
	@Override
	public void handleDELETE(CoapExchange request) {
		try {
			deleteConfig();
			request.respond(ResponseCode.DELETED, "app "+getName()+" has been deleted");
		} catch (IOException e) {
			e.printStackTrace();
			request.respond(ResponseCode.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Delete this config, the resource that belongs to it and fire a
	 * notification so that AppResource deletes the app instance's root resource
	 * as well.
	 */
	@Override
	public void deleteConfig() throws IOException {
		setProperty(RUNNING, STOP);
		setProperty(AVAILABILITY, UNABAILABLE);
		fireNotification(new ConfigChangeSet(RUNNING, AVAILABILITY));
		super.deleteConfig();
	}
	
	/**
	 * Initialize the AppConfig with some default values.
	 */
	private void init() {
		setProperty(TYPE, AppType.JAVASCRIPT);
		setProperty(NAME, UNNAMED);
		setProperty(START_ON_STARTUP, true);
		setProperty(ALLOW_OUTPUT, true);
		setProperty(ALLOW_ERROR_OUTPUT, true);
		setProperty(ENABLE_REQUEST_DELIVERY, true);
		setProperty(RUNNING, STOP);
		setProperty(DIR_PATH, "appserver/installed/");
		setProperty(AVAILABILITY, AVAILABLE);
	}
	
	private void makeAllKeysToLowercase() {
		for (Object key:this.keySet().toArray()) {
			Object value = this.remove(key);
			this.put(key.toString().toLowerCase(), value);
		}
	}
}

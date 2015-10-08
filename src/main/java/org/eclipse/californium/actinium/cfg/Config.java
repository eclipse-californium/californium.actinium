/*******************************************************************************
 * Copyright (c) 2015 Institute for Pervasive Computing, ETH Zurich and others.
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

/**
 * The Config contains all properties that hold globally for the whole app
 * server.
 */
public class Config extends AbstractConfig {

	private static final long serialVersionUID = 1132829953418308387L;

	// global configuration constants
	public static final String PATH = "appserver/config.cfg";
	
	// keys
	public static final String APP_CONFIG_PREFIX = "app_config_prefix";
	public static final String APP_CONFIG_SUFFIX = "app_config_suffix";
	public static final String JAVASCRIPT_SUFFIX = "javascript_suffix";
	
	public static final String START_ON_INSTALL = "start_on_install"; // true, if app instances shall start as soon as installed
	
	public static final String APP_PATH = "app_path"; // path to the apps
	public static final String APP_CONFIG_PATH = "app_config_path"; // path to the configs of apps (which containt the filename)
	public static final String APP_CONFIG_RESOURSES = "app_config_resources";

	public static final String APPS_RESOURCE_ID = "apps_resource_id"; // identifier of AppResource
	public static final String CONFIG_RESOURCE_ID = "config_resource_id"; // identifier of this config's resource
	public static final String INSTALL_RESOURCE_ID = "install_resource_id"; // identifier of InstallResource
	public static final String RUNNING_RESOURCE_ID = "running_resource_id"; // identifier of RunningResource
	public static final String STATS_RESOURCE_ID = "stats_resource_id"; // identifier of StatsResource
	
	/**
	 * Constructs a new Config from the default path
	 */
	public Config() {
		this(PATH);
	}
	
	/**
	 * Constructs a new Config from the specified filepath
	 * @param path the path to the config file
	 */
	public Config(String path) {
		super(path);
		init();
		loadProperties(path);
	}
	
	private void init() {
		setProperty(APP_PATH, "appserver/installed/");
		setProperty(APP_CONFIG_PATH, "appserver/apps/");
		setProperty(JAVASCRIPT_SUFFIX, ".js");
		setProperty(APP_CONFIG_SUFFIX, ".cfg");
		setProperty(APP_CONFIG_PREFIX, "config_");
		
		setProperty(START_ON_INSTALL, false);

		setProperty(CONFIG_RESOURCE_ID, "config");
		setProperty(INSTALL_RESOURCE_ID, "install");
		setProperty(APPS_RESOURCE_ID, "apps");
		setProperty(APP_CONFIG_RESOURSES, "instances");
		setProperty(RUNNING_RESOURCE_ID, "running");
		setProperty(STATS_RESOURCE_ID, "stats");
	}
}

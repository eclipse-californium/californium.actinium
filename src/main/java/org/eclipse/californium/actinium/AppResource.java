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

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.californium.actinium.cfg.AbstractConfig.ConfigChangeSet;
import org.eclipse.californium.actinium.cfg.AppConfig;
import org.eclipse.californium.actinium.cfg.AppConfigsResource;
import org.eclipse.californium.actinium.cfg.Config;
import org.eclipse.californium.actinium.plugnplay.AbstractApp;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

/**
 * AppResource is the root of all resources around instances of apps. It
 * contains an AppConfigsResource with the configs of all app instances and a
 * RunningsResources with all running instances. Furthermore it contains the app
 * server's config resource with the properties for the whole app server and the
 * StatsResource with stats abaout all apps.
 * <p>
 * When an instance of an app is created it will be added to AppResource. The
 * app's properties like name, type and more are stored in a AppsCOnfig. The
 * AppConfigs of the apps are added to the AppConfigsResource. AppResource also
 * adds an observer to the configs. When an app is running, it is added to the
 * RunningResource. If it is stopped, AppResource removes it from it. When an
 * app restarts, AppResource does nothing.
 * <p>
 * On a GET request, AppResource returns a list of all running apps. POST, PUT
 * and DELETE requests are not allowed.
 */
public class AppResource extends CoapResource {

	private AppManager manager;
	
	// the list of all apps
	private List<AbstractApp> apps;
	
	// the resource that holds the app's configs
	private AppConfigsResource appConfigsRes;
	
	// the resource that holds the running apps
	private RunningResource runningRes;

	/**
	 * Constructs a new AppResource with the specified app server config and
	 * manager. First it creates the AppConfigsResource and RunningResource for
	 * the app's configs and the running apps respectively. Then it loads all
	 * apps from the disk and adds them to the RunningResource and their configs
	 * to the AppCOnfigResource.
	 * 
	 * @param manager the AppManager
	 */
	public AppResource(AppManager manager) {
		super(manager.getConfig().getProperty(Config.APPS_RESOURCE_ID), false);
		
		this.manager = manager;
		
		this.apps = new LinkedList<AbstractApp>();
		
		this.appConfigsRes = new AppConfigsResource(manager.getConfig().getProperty(Config.APP_CONFIG_RESOURSES));
		add(appConfigsRes);
		
		this.runningRes = new RunningResource(manager.getConfig());
		add(runningRes);
		
		List<AbstractApp> allapps = manager.loadAllApps();
		for (AbstractApp app:allapps) {
			addApp(app);
		}

		manager.setAppResource(this);
		
		System.out.println("Application resource is ready");
	}
	
	public void startApps() {
		for (AbstractApp app:apps) {
			if (app.getConfig().getBool(AppConfig.START_ON_STARTUP)) {
				app.start();
			}
		}
	}

	/**
	 * Stop all apps.
	 */
	public void stopApps() {
		for (AbstractApp app:apps) {
			app.getConfig().setPropertyAndNotify(AppConfig.RUNNING, AppConfig.STOP);
		}
	}

	/**
	 * Return a list of all running apps.
	 */
	@Override
	public void handleGET(CoapExchange request) {
		runningRes.handleGET(request);
	}
	
	/**
	 * Returns a list of all available apps' names.
	 * @return a list of all available apps' names.
	 */
	public String[] getAppsNames() {
		String[] names = new String[apps.size()];
		for (int i=0;i<names.length;i++) {
			names[i] = apps.get(i).getName();
		}
		return names;
	}
	
	public AbstractApp getApp(String appname) {
		if (appname==null) return null;
		for (AbstractApp app:apps) {
			if (appname.equals(app.getName()))
				return app;
		}
		return null;
	}
	
	/**
	 * Returns an array of all available apps
	 * @return an array of all available apps
	 */
	public AbstractApp[] getAllApps() {
		return apps.toArray(new AbstractApp[apps.size()]);
	}

	/**
	 * Restarts all instance of the app with the given name, that are started at
	 * the moment. Does not change the apps that are stopped.
	 * 
	 * @param appname the app's name
	 */
	public void restartApps(String appname) {
		for (AbstractApp app:apps) {
			if (appname.equals(app.getConfig().getProperty(AppConfig.APP))
					&& app.getConfig().getProperty(AppConfig.RUNNING).equals(AppConfig.START)) {
				app.getConfig().setPropertyAndNotify(AppConfig.RUNNING, AppConfig.RESTART);
			}
		}
	}

	/**
	 * Deletes all instances of the app with the specified name plus their
	 * configs. Those which are running at the moment, get stopped first.
	 * 
	 * @param appname the name of the app
	 * @throws IOException
	 *             if the deletion process fails, e.g. file not found or not
	 *             accessible.
	 */
	public void deleteApps(String appname) throws IOException {
		for (AbstractApp app:apps.toArray(new AbstractApp[apps.size()])) {
			AppConfig appcfg = app.getConfig();
			if (appname.equals(appcfg.getProperty(AppConfig.APP))) {
				if (appcfg.getProperty(AppConfig.RUNNING).equals(AppConfig.START) 
						|| appcfg.getProperty(AppConfig.RUNNING).equals(AppConfig.RESTART)) {
					appcfg.setPropertyAndNotify(AppConfig.RUNNING, AppConfig.STOP);
				}
				appcfg.deleteConfig();
			}
		}
	}

	/**
	 * Adds a new app to the available apps.
	 * 
	 * @param app the app
	 */
	public void installApp(AbstractApp app) {
		addApp(app);
	}
	
	// Implementation for the process of adding an app
	/**
	 * Add the specified app to the available apps. AppResource adds the app to
	 * its list of apps and adds their configs to the AppConfigsResource.
	 * Finally it adds an Observer to the app that listens to different changes
	 * in the app config.
	 */
	private void addApp(final AbstractApp app) {
		
		// add app to internal list
		apps.add(app);
		// set path for headless resource
		app.setPath(runningRes.getURI() + "/");
		
		// add app's config with given resid
		final AppConfig appconfig = app.getConfig();
		appConfigsRes.addConfig(appconfig);
		
		/*
		 * On a change of APP: restart app with new app.
		 * On a change of RUNNING: remove or add app to RunningResource
		 * On a change of AVAILABILITY: remove app from list of apps (App has been deleted)
		 */
		appconfig.getObservable().addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				if (!(arg instanceof AppConfig.ConfigChangeSet))
					return;
				ConfigChangeSet set = (ConfigChangeSet) arg;
				if (set.contains(AppConfig.APP)) {
					app.restart();
				}
				if (set.contains(AppConfig.RUNNING)) {
					String running = appconfig.getProperty(AppConfig.RUNNING);
					if (running.equals(AppConfig.START)) {
						runningRes.addApp(app);
					} else if (running.equals(AppConfig.STOP)) {
						app.delete();
					}
				}
				if (set.contains(AppConfig.AVAILABILITY)) {
					if (appconfig.getProperty(AppConfig.AVAILABILITY).equals(AppConfig.UNABAILABLE)) {
						// app has been removed. Also remove it from list of apps
						apps.remove(app);
					}
				}
			}
		});
	}
	
	/**
	 * Checks, whether the specified id is already in use. If so, add a number
	 * that makes the id unique.
	 */
	@SuppressWarnings("unused")
	private String createUniqueResourceId(String resid) {
		HashSet<String> allids = new HashSet<String>();
		for (AbstractApp a:apps)
			allids.add(a.getName());
		
		for (int i=2;i<allids.size()+1+2;i++) {
			String test = resid +"(" + i + ")";
			if (!allids.contains(test))
				return test;
		}
		return null; // not possible to get here
	}

	public void restartAppsByLibraryName(String libname) {
		for (AbstractApp app:apps) {
			if (app.dependencies.contains(libname)
					&& app.getConfig().getProperty(AppConfig.RUNNING).equals(AppConfig.START)) {
				app.getConfig().setPropertyAndNotify(AppConfig.RUNNING, AppConfig.RESTART);
			}
		}
	}
}

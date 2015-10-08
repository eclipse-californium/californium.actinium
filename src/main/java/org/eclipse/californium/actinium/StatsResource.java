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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.californium.actinium.cfg.Config;
import org.eclipse.californium.actinium.plugnplay.AbstractApp;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * Statsresource holds the stats of all app instances and their subresources.
 */
public class StatsResource extends CoapResource {

	// the app server's config
	private Config config;
	
	// the AppManager to get retrieve information from the apps
	private AppManager manager;
	
	private HashMap<String, AppInfo> appinfos; // maps app names to their info
	private HashMap<String, ResourceInfo> resinfos; // maps resource paths to their info

	private AppInfo allinfo;
	
	/**
	 * Constructs a new StatsResource with the specified app server config and
	 * the specified AppManager.
	 * 
	 * @param config the app server's config
	 * @param manager the AppManager
	 */
	public StatsResource(Config config, AppManager manager) {
		super(config.getProperty(Config.STATS_RESOURCE_ID));
		this.config = config;
		this.manager = manager;
		this.manager.setStatsResource(this);
		
		this.allinfo = new AppInfo();
		this.appinfos = new HashMap<String, StatsResource.AppInfo>();
		this.resinfos = new HashMap<String, StatsResource.ResourceInfo>();
		
		/*
		 * Use the key null for all requests that do not go to an app but to
		 * another resource. Since apps might have any possible name null is the
		 * only unused name. Otherwise we could also not count the requests to
		 * non-apps resources or use a prefix for every app inside the HashMap.
		 */
		appinfos.put(null, new AppInfo());
		
		// create AppInfos for all installed apps
		AbstractApp[] apps = manager.getAllApps();
		for (AbstractApp app:apps) {
			oninstallApp(app.getName());
		}
	}

	/**
	 * Records the specified request to the specified resource to count the GET,
	 * POST, PUT and DELETE requests the resource receives.
	 * 
	 * @param request the request.
	 * @param resource the resource to which the request is sent.
	 */
	public void record(Request request, Resource resource) {
		String appname = getAppName(resource);

		// if appname==null use the appinfo for all non-app resources
		AppInfo appinfo = appinfos.get(appname);
		
		String path = resource.getURI();
		ResourceInfo resinfo = resinfos.get(path);
		if (resinfo==null) { 
			// if no information about this resource available, create a new one
			resinfo = new ResourceInfo(appname);
			resinfos.put(path, resinfo);
		}
		
		int payloadSize = request.getPayload().length;
		allinfo.payloadsum += payloadSize;
		appinfo.payloadsum += payloadSize;
		resinfo.payloadsum += payloadSize;
		
		if (request.getCode() == Code.GET) {
			allinfo.getreqcount++;
			appinfo.getreqcount++;
			resinfo.getreqcount++;
		} else if (request.getCode() == Code.POST) {
			allinfo.postreqcount++;
			appinfo.postreqcount++;
			resinfo.postreqcount++;
		} else if (request.getCode() == Code.PUT) {
			allinfo.putreqcount++;
			appinfo.putreqcount++;
			resinfo.putreqcount++;
		} else if (request.getCode() == Code.DELETE) {
			allinfo.deletereqcount++;
			appinfo.deletereqcount++;
			resinfo.deletereqcount++;
		}
	}

	/**
	 * Removes all informations about instances of the app with the specified
	 * name.
	 * 
	 * @param name the name of the app.
	 */
	public void ondeleteApp(String name) {
		if (name==null) return;
		appinfos.remove(name);
		for (String key:resinfos.keySet()) {
			if (name.equals(resinfos.get(key).appname))
				appinfos.remove(key);
		}
	}

	/**
	 * Adds a new information holder for a newly created app instance with the
	 * specified name.
	 * 
	 * @param name the name of the app instance.
	 */
	public void oninstallApp(String name) {
		AppInfo appinfo = new AppInfo();
		appinfos.put(name, appinfo);
	}

	/**
	 * Responses with all stored information about all app instances and all
	 * their subresources.
	 */
	@Override
	public void handleGET(CoapExchange request) {
		StringBuffer buffer = new StringBuffer();

		AbstractApp[] apps = manager.getAllApps();
		
		buffer.append("All apps: ("+apps.length+")");
		buffer.append("\n\tGET requests: "+allinfo.getreqcount);
		buffer.append("\n\tPOST requests: "+allinfo.postreqcount);
		buffer.append("\n\tPUT requests: "+allinfo.putreqcount);
		buffer.append("\n\tDELETE requests: "+allinfo.deletereqcount);
		buffer.append("\n\tPayload: "+allinfo.payloadsum+" bytes");
		
		for (AbstractApp app:apps) {
			String appname = app.getName();
			buffer.append("\n"+appname+":");
			
			addThreadInfos(app, buffer);
			
			AppInfo appinfo = appinfos.get(appname);
			buffer.append("\n\tGET requests: "+appinfo.getreqcount);
			buffer.append("\n\tPOST requests: "+appinfo.postreqcount);
			buffer.append("\n\tPUT requests: "+appinfo.putreqcount);
			buffer.append("\n\tDELETE requests: "+appinfo.deletereqcount);
			buffer.append("\n\tPayload: "+appinfo.payloadsum+" bytes");
			
			addRequestCounter(app, buffer);
		}
		request.respond(ResponseCode.CONTENT, buffer.toString());
	}

	/**
	 * Add information about the Threads executing the apps. This might be
	 * removed if not useful.
	 */
	private void addThreadInfos(AbstractApp app, StringBuffer buffer) {
		long startts = app.getStartTimestamp();
		long stopts = app.getStopTimestamp();
		if (startts==0) {
			buffer.append("\n\tStart time: not yet");
		} else {
			buffer.append("\n\tStart time: "+new SimpleDateFormat("kk:mm:ss - dd.MM.yyyy").format(new Date(startts)));
		}
		if (stopts==0) {
			buffer.append("\n\tStop time: not yet");
		} else {
			buffer.append("\n\tStop time: "+new SimpleDateFormat("kk:mm:ss - dd.MM.yyyy").format(new Date(stopts)));
		}
		
		long id = app.getRunningThreadId();
		if (id==-1) {
			buffer.append("\n\tExecuting thread: not available");
		} else {
			buffer.append("\n\tExecuting thread id: "+id);
			ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
			if (threadBean.isThreadCpuTimeSupported() && threadBean.isThreadCpuTimeEnabled()) {
				long cputime = threadBean.getThreadCpuTime(id);
				if (cputime==-1) {
					buffer.append("\n\tExecuting thread cpu time: already finished");
				} else {
					buffer.append("\n\tExecuting thread cpu time: "+cputime/1000000000d+" s");
				}
			} else {
				buffer.append("\n\tExecuting thread cpu time: not available");
			}
		}
	}

	/**
	 * Recursively adds the reqeust counters for every subresource of the given
	 * resource
	 * 
	 * @param res - the resource
	 * @param buffer - the buffer to which the text is added to
	 */
	private void addRequestCounter(Resource res, StringBuffer buffer) {
		String respath = res.getURI();
		buffer.append("\n\t"+respath+":");
		ResourceInfo resinfo = resinfos.get(respath);
		if (resinfo==null) {
			buffer.append("\n\t\tGET requests: 0" +
					"\n\t\tPOST requests: 0" +
					"\n\t\tPUT requests: 0" +
					"\n\t\tDELETE requests: 0" +
					"\n\t\tPayload: 0 bytes");
		} else {
			buffer.append("\n\t\tGET request: "+resinfo.getreqcount);
			buffer.append("\n\t\tPOST request: "+resinfo.postreqcount);
			buffer.append("\n\t\tPUT request: "+resinfo.putreqcount);
			buffer.append("\n\t\tDELETE request: "+resinfo.deletereqcount);
			buffer.append("\n\t\tPayload: "+resinfo.payloadsum+" bytes");
		}
		for (Resource r:res.getChildren()) {
			addRequestCounter(r,buffer);
		}
	}

	/**
	 * Returns the name of the app instance to which the specified resource
	 * belongs to or null if it corresponds to no app instance.
	 * 
	 * @param res the resource.
	 * @return the app instance it belongs to or null.
	 */
	private String getAppName(Resource res) {
		// path in this form: /apps/running/appname/...
		String path = res.getURI();
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
	 * Holds the information about an app instance, i.e. the counters for
	 * received GET, POST, PUT and DELETE requests to the app instance's root
	 * resource directly or its subresources.
	 */
	private class AppInfo {
		private int getreqcount;
		private int postreqcount;
		private int putreqcount;
		private int deletereqcount;
		private int payloadsum;
	}

	/**
	 * Holds the information about a resource, i.e. the counters for received
	 * GET, POST, PUT and DELETE requests and the name of the app the resource
	 * belongs to.
	 */
	private class ResourceInfo {
		private String appname; // null if this info doesn't correspond to any app
		private int getreqcount;
		private int postreqcount;
		private int putreqcount;
		private int deletereqcount;
		private int payloadsum;
		
		private ResourceInfo(String appname) {
			this.appname = appname;
		}
	}
	
}

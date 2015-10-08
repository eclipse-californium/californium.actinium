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

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * AppConfigResource contains the app's configs' resources. When an app is
 * created, the corresponding resource will be added to the AppConfigsResource.
 * <p>
 * The parent of a AppConfigsResource is supposed to be an AppResource. When an
 * app gets deleted, all configs correspondig to this app will be removed from
 * AppConfigsResource by its AppResource parent.
 * <p>
 * On a GET reqeust, it AppConfigResource lists all configs it contains. POST,
 * PUT and DELETE are not allowed.
 */
public class AppConfigsResource extends CoapResource {

	// a list of all resources correspondig to one app's config each
	private List<AppConfig> appconfigs;
	
	/**
	 * Create a new AppConfigsResource with the specified resource identifier.
	 * @param resourceIdentifier the identifier for this resource
	 */
	public AppConfigsResource(String resourceIdentifier) {
		super(resourceIdentifier);
		this.appconfigs = new ArrayList<AppConfig>();
	}

	/**
	 * Adds a config, creates a correspondig resource and adds it as
	 * subresource.
	 * 
	 * @param appconfig the configuration
	 */
	public void addConfig(final AppConfig appconfig) {
		String identifier = appconfig.getName();
		appconfigs.add(appconfig);
		
		final Resource res = appconfig.createConfigResource(identifier);

		/*
		 * On a change of AVAILABILITY: remove app conf from list of app configurations (App has been deleted)
		 */
		appconfig.getObservable().addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				if (!(arg instanceof AppConfig.ConfigChangeSet))
					return;
				AbstractConfig.ConfigChangeSet set = (AbstractConfig.ConfigChangeSet) arg;
				if (set.contains(AppConfig.AVAILABILITY)) {
					if (appconfig.getProperty(AppConfig.AVAILABILITY).equals(AppConfig.UNABAILABLE)) {
						// app has been removed. Also remove it from list of apps
						appconfigs.remove(appconfig);
						AppConfigsResource.this.delete(res);
					}
				}
			}
		});
		add(res);
	}

	/**
	 * Respond a list of all configs.
	 */
	@Override
	public void handleGET(CoapExchange request) {
		Response response = new Response(ResponseCode.CONTENT);

		StringBuffer buffer = new StringBuffer();
		buffer.append("Apps have the following configurations:\n");
		for (AppConfig appcfg:appconfigs) {
			String name = appcfg.getName();
			String cfgresid = appcfg.getConfigResource().getURI();
			buffer.append("	"+name+": "+cfgresid+"\n");
		}
		
		response.setPayload(buffer.toString());
		request.respond(response);
	}

	@Override
	public void handlePUT(CoapExchange request) {
		request.respond(ResponseCode.METHOD_NOT_ALLOWED);
	}

	@Override
	public void handlePOST(CoapExchange request) {
		request.respond(ResponseCode.METHOD_NOT_ALLOWED);
	}

	@Override
	public void handleDELETE(CoapExchange request) {
		request.respond(ResponseCode.METHOD_NOT_ALLOWED);
	}

}

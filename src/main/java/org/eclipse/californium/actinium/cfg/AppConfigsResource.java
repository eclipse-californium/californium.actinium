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

import ch.ethz.inf.vs.californium.coap.CodeRegistry;
import ch.ethz.inf.vs.californium.coap.DELETERequest;
import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.POSTRequest;
import ch.ethz.inf.vs.californium.coap.PUTRequest;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.endpoint.LocalResource;
import ch.ethz.inf.vs.californium.endpoint.Resource;

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
public class AppConfigsResource extends LocalResource {

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
	 * @param appconfig
	 */
	public void addConfig(AppConfig appconfig) {
		String identifier = appconfig.getName();
		appconfigs.add(appconfig);
		
		Resource res = appconfig.createConfigResource(identifier);
		add(res);
	}
	
	/**
	 * Respond a list of all configs.
	 */
	@Override
	public void performGET(GETRequest request) {
		Response response = new Response(CodeRegistry.RESP_CONTENT);

		StringBuffer buffer = new StringBuffer();
		buffer.append("Apps have the following configurations:\n");
		for (AppConfig appcfg:appconfigs) {
			String name = appcfg.getName();
			String cfgresid = appcfg.getConfigResource().getPath();
			buffer.append("	"+name+": "+cfgresid+"\n");
		}
		
		response.setPayload(buffer.toString());
		request.respond(response);
	}

	@Override
	public void performPUT(PUTRequest request) {
		request.respond(CodeRegistry.RESP_METHOD_NOT_ALLOWED);
	}

	@Override
	public void performPOST(POSTRequest request) {
		request.respond(CodeRegistry.RESP_METHOD_NOT_ALLOWED);
	}

	@Override
	public void performDELETE(DELETERequest request) {
		request.respond(CodeRegistry.RESP_METHOD_NOT_ALLOWED);
	}

}

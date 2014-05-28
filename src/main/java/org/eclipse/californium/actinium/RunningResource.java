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

import org.eclipse.californium.actinium.cfg.Config;
import org.eclipse.californium.actinium.plugnplay.AbstractApp;

import ch.ethz.inf.vs.californium.coap.CodeRegistry;
import ch.ethz.inf.vs.californium.coap.DELETERequest;
import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.POSTRequest;
import ch.ethz.inf.vs.californium.coap.PUTRequest;
import ch.ethz.inf.vs.californium.endpoint.LocalResource;
import ch.ethz.inf.vs.californium.endpoint.Resource;

/**
 * The RunningResource contains all running apps. RunningResource's parent is
 * supposed to be an AppResource which has a reference to this resource. When an
 * app starts, the AppResource adds the app, which is also a resource, to the
 * RunningResource. When an app stops, the AppResource removes it again. When an
 * app restarts it remains untouched.
 * <p>
 * On a GET request, RunningResource lists all running apps. POST, PUT and
 * DELETE Requests are not allowed
 */
public class RunningResource extends LocalResource {

	/**
	 * Constructs a new RunningResource with the resource identifier specified in the given config.
	 * @param config the config with the desired resource identifier for this resource.
	 */
	public RunningResource(Config config) {
		super(config.getProperty(Config.RUNNING_RESOURCE_ID));
	}
	
	/**
	 * Adds the given app as subresource.
	 * @param app an app.
	 */
	public void addApp(AbstractApp app) {
		add(app);
	}
	
	/**
	 * Responds with a list of all running apps.
	 */
	@Override
	public void performGET(GETRequest request) {
		StringBuffer buffer = new StringBuffer();
		for (Resource res:getSubResources())
			buffer.append(res.getName()+"\n");
		
		request.respond(CodeRegistry.RESP_CONTENT, buffer.toString());
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

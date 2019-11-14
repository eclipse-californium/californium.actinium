/*******************************************************************************
 * Copyright (c) 2014 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Martin Lanter - architect and initial implementation
 ******************************************************************************/
package org.eclipse.californium.actinium.jscoap;

import jdk.nashorn.api.scripting.NashornException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

/**
 * It is not possible to add further methods or fields to this class within
 * JavaScript (Rhino). If this is necessary, use AbstractJavaScriptResource.
 */
public class JavaScriptResource extends CoapResource implements JavaScriptCoapConstants {
	// Cannot extend ScriptableObject, because has to extend CoapResource
	// Cannot (reasonably) implement Scriptable, because we then have to implement all 16 methods like ScriptableObject

	public CoapCallback onget = null;
	public CoapCallback onpost = null;
	public CoapCallback onput = null;
	public CoapCallback ondelete = null;
	
	public JavaScriptResource() {
		super(null);
	}

	public JavaScriptResource(String resourceIdentifier) {
		super(resourceIdentifier);
	}
	
	public JavaScriptResource(String resourceIdentifier, boolean hidden) {
		super(resourceIdentifier, hidden);
	}

	public void setTitle(String resourceTitle){
		getAttributes().setTitle(resourceTitle);
	}

	@Override
	public void changed() {
		super.changed();
	}
	
	public CoapCallback getOnget() {
		return onget;
	}
	
	public CoapCallback getOnpost() {
		return onpost;
	}
	
	public CoapCallback getOnput() {
		return onput;
	}
	
	public CoapCallback getOndelete() {
		return ondelete;
	}
	
	public Object getThis() {
		return this;
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {
		CoapCallback onget = getOnget();
		if (onget!=null) {
			callJSCallback(exchange, onget);
		} else {
			super.handleGET(exchange);
		}
	}

	private void callJSCallback(CoapExchange exchange, CoapCallback callback) {
		try {
			callback.call(new JavaScriptCoapExchange(exchange), exchange.advanced().getRequest());
		}catch (NashornException e){
			exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR, e.getMessage());
			System.err.println("JavaScript error in ["+e.getFileName()+"#"+e.getLineNumber()+"]: "+e.getMessage());
			throw e;
		}
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		CoapCallback onpost = getOnpost();
		if (onpost!=null) {
			callJSCallback(exchange, onpost);
		} else {
			super.handlePOST(exchange);
		}
	}

	@Override
	public void handlePUT(CoapExchange exchange) {
		CoapCallback onput = getOnput();
		if (onput!=null) {
			callJSCallback(exchange, onput);
		} else {
			super.handlePUT(exchange);
		}
	}

	@Override
	public void handleDELETE(CoapExchange exchange) {
		CoapCallback ondelete = getOndelete();
		if (ondelete!=null) {
			callJSCallback(exchange, ondelete);
		} else {
			super.handleDELETE(exchange);
		}
	}

}

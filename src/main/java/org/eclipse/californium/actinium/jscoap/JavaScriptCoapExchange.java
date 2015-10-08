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
package org.eclipse.californium.actinium.jscoap;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

public class JavaScriptCoapExchange extends ScriptableObject implements JavaScriptCoapConstants {

	private static final long serialVersionUID = 2269672652051004591L;

	private CoapExchange exchange;
	private Request request;
	
	private OptionSet responseOptions = new OptionSet();
	
	/*
	 * Rhino: Needs an empty constructor for ScriptableObjects
	 */
	public JavaScriptCoapExchange() {
		// do nothing
	}
	
	public JavaScriptCoapExchange(CoapExchange exchange) {
		this.exchange = exchange;
		this.request = exchange.advanced().getRequest();
	}
	
	@Override
	public String getClassName() {
		return "JavaScriptCoapExchange";
	}
	
	/*
	 * Rhino: Needs JavaScript constructor
	 */
    public void jsConstructor() {
    }
	
    // Fields for JavaScript
    
    public String jsGet_requestType() {
    	return MediaTypeRegistry.toString(request.getOptions().getContentFormat());
	}
	
    public String jsGet_requestText() {
		return request.getPayloadString();
	}

    public byte[] jsGet_request() {
		return request.getPayload();
	}
	
	// Functions for JavaScript
	
	public byte[] jsFunction_getPayload() {
		return request.getPayload();
	}
	
	public int jsFunction_payloadSize() {
		return request.getPayloadSize();
	}
	
	public void jsFunction_accept() {
		exchange.accept();
	}
	
	/*
	 * Rhino: Only one method jsFunction_respond is allowed
	 */
	public void jsFunction_respond(Object jscode, Object jsmessage, Object jscontentType) {
		respond(jscode, jsmessage, jscontentType);
	}
	
	
	public int jsFunction_getMID() {
		return request.getMID();
	}
	
	public String jsFunction_getUriPath() {
		return request.getOptions().getUriPathString();
	}
	
	public String jsFunction_getQuery() {
		return request.getOptions().getUriQueryString();
	}
	
	public int jsFunction_getContentType() {
		return request.getOptions().getContentFormat();
	}
	
	public String jsFunction_getTokenString() {
		return request.getTokenString();
	}
	
	public int jsFunction_getMaxAge() {
		return request.getOptions().getMaxAge().intValue();
	}
	
	public String jsFunction_getLocationPath() {
		return request.getOptions().getLocationPathString();
	}
	
	public void jsFunction_setLocationPath(String locationPath) {
		responseOptions.setLocationPath(locationPath);
	}
	
	public Type jsFunction_getType() {
		return request.getType();
	}
	
	public long jsFunction_getTimestamp() {
		return request.getTimestamp();
	}
	
	public boolean jsFunction_isConfirmable() {
		return request.isConfirmable();
	}
	
	public boolean jsFunction_isNonConfirmable() {
		return request.getType() == Type.NON;
	}
	
	public boolean jsFunction_isAcknowledgement() {
		return request.getType() == Type.ACK;
	}

	public boolean jsFunction_isReset() {
		return request.getType() == Type.RST;
	}
	
	public boolean jsFunction_isEmptyACK() {
		return request.getType() == Type.ACK && request.getPayloadSize() == 0;
	}
	
	public String jsFunction_toString() {
		return request.toString();
	}
	
	// options
	public void jsFunction_setResponseHeader(String option, Object value)  {
		
		// convenience hack
		if ("Content-Type".equals(option)) option = "Content-Format";
		
		int nr = OptionNumberRegistry.toNumber(option);
		
		if (value instanceof Integer) {
			setResponseHeader(nr, (Integer) value);
		} else if (value instanceof String) {
			setResponseHeader(nr, (String) value);
		} else {
			setResponseHeader(nr, value.toString());
		}
	}
	
	public String jsFunction_getAllRequestHeaders() {
		return getAllRequestHeaders();
	}
		
	private void setResponseHeader(int nr, String value)  {
		if (nr==OptionNumberRegistry.CONTENT_FORMAT || nr==OptionNumberRegistry.ACCEPT) {
			// we also have to parse the value to get it as integer
			int contentFormat = MediaTypeRegistry.parse(value);
			if (contentFormat > MediaTypeRegistry.UNDEFINED) responseOptions.addOption(new Option(nr, contentFormat));
		} else {
			responseOptions.addOption(new Option(nr, value));
		}
	}
	
	private void setResponseHeader(int nr, int value)  {
		responseOptions.addOption(new Option(nr, value));
	}
	
	private String getAllRequestHeaders() {
		return request.getOptions().toString();
	}
	
	private void respond(Object jsCode, Object jsMessage, Object jsContentFormat) {

		Integer code;

		// Parse code (e.g. 69, 2.05 or "Content")
		
		if (jsCode instanceof Integer) {
			code = (Integer) jsCode;
			// Problem: 4.00 becomes the Integer 4 -> convert 1--5 to actual raw value code
			if (code <= 5) {
				code *= 32; 
			}
		} else if (jsCode instanceof String) {
			code = CoAPConstantsConverter.convertStringToCode((String) jsCode);
		} else if (jsCode instanceof Double) {
			code = CoAPConstantsConverter.convertNumCodeToCode((Double) jsCode);
		} else if (jsCode instanceof Float) {
			code = CoAPConstantsConverter.convertNumCodeToCode(((Float) jsCode).doubleValue());
		} else {
			throw new IllegalArgumentException( "JavaScriptCoapExchange.respond expects a String, Integer or Double as first argument but got "+jsCode);
		}

		// Parse content format (e.g. "text/plain", 0 or nothing)
		if (jsContentFormat instanceof Integer)
			responseOptions.setContentFormat((Integer) jsContentFormat);
		else if (jsContentFormat instanceof String)
			responseOptions.setContentFormat(MediaTypeRegistry.parse((String) jsContentFormat));

		// Respond to the request
		Response response = new Response(ResponseCode.valueOf(code));
		response.setOptions(responseOptions);
		if (jsMessage != null && !(jsMessage instanceof Undefined)) response.setPayload(Context.toString(jsMessage));
		
		exchange.respond(response);
	}
	
}

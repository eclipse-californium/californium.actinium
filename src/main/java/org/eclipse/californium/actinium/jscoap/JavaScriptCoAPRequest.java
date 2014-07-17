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
package org.eclipse.californium.actinium.jscoap;

import java.util.List;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

public class JavaScriptCoAPRequest extends ScriptableObject implements CoAPConstants {

	private static final long serialVersionUID = 2269672652051004591L;

	private CoapExchange exchange;
	private Request request;
	
	private Response response /* = new Response()*/;
	
	/*
	 * Rhino: Needs an empty constructor for ScriptableObjects
	 */
	public JavaScriptCoAPRequest() {
		// do nothing
	}
	
	public JavaScriptCoAPRequest(CoapExchange exchange) {
		this.exchange = exchange;
		this.request = exchange.advanced().getRequest();
	}
	
	@Override
	public String getClassName() {
		return "JavaScriptCoAPRequest";
	}
	
	/*
	 * Rhino: Needs JavaScript constructor
	 */
    public void jsConstructor() {
    }
	
    // Fields for JavaScript //
	
    public String jsGet_payloadText() {
		return request.getPayloadString();
	}
	
//	public long jsGet_startTime() {
//		return request.startTime;
//	}
	
	// Functions for JavaScript //

	public String jsFunction_getPayloadString() {
		return request.getPayloadString();
	}
	
	public void jsFunction_accept() {
		exchange.accept();
	}
	
	/*
	 * Rhino: Only one method jsFunction_respond is allowed
	 */
	public void jsFunction_respond(Object jscode, Object jsmessage, Object jscontentType) {
		respond(jscode, Context.toString(jsmessage), jscontentType);
	}
	
	public void jsFunction_sendResponse() {
		//request.sendResponse();
	}
	
	
	public String jsFunction_getPayload() {
		return request.getPayloadString();
	}
	
	public int jsFunction_payloadSize() {
		return request.getPayloadSize();
	}
	
//	public int jsFunction_getVersion() {
//		return request.getVersion();
//	}
	
	public int jsFunction_getMID() {
		return request.getMID();
	}
	
	public void jsFunction_setMID(int mid) {
		response.setMID(mid);
	}
	
	public String jsFunction_getUriPath() {
		return request.getOptions().getURIPathString();
	}
	
	public String jsFunction_getQuery() {
		return request.getOptions().getURIQueryString();
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
		response.getOptions().setLocationPath(locationPath);
	}

//	public String jsFunction_key() {
//		return request.key();
//	}
//	
//	public String jsFunction_transactionKey() {
//		return request.transactionKey();
//	}
//	
//	public String jsFunction_sequenceKey() {
//		return request.sequenceKey();
//	}
	
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
	
//	public boolean jsFunction_isReply() {
//		return request.isReply();
//	}
	
	public boolean jsFunction_isEmptyACK() {
		return request.getType() == Type.ACK && request.getPayloadSize() == 0;
	}
	
//	public boolean jsFunction_requiresToken() {
//		return request.requiresToken();
//	}
	
	public String jsFunction_toString() {
		return request.toString();
	}
	
//	public String jsFunction_typeString() {
//		return request.typeString();
//	}
	
	// options
	public void jsFunction_setResponseHeader(String header, Object value)  {
		if (value instanceof Integer)
			setResponseHeader(header, (Integer) value);
		else if (value instanceof String)
			setResponseHeader(header, (String) value);
		else
			setResponseHeader(header, value.toString());
	}
	
	public String jsFunction_getAllRequestHeaders() {
		return getAllRequestHeaders();
	}
	
//	public String jsFunction_CoapExchangeHeader(String header) {
//		return CoapExchangeHeader(header);
//	}
		
	private void setResponseHeader(String header, String value)  { // TODO: test if this works
		int nr = CoAPConstantsConverter.convertHeaderToInt(header);
		if (nr==OptionNumberRegistry.CONTENT_TYPE) {
			// we also have to parse the value to get it as integer
			int contentType = CoAPConstantsConverter.convertStringToContentType(value);
			response.getOptions().addOption(new Option(nr, contentType));
		} else if (nr==OptionNumberRegistry.ACCEPT) {
			// we also have to parse the value to get it as integer
			int contentType = CoAPConstantsConverter.convertStringToContentType(value);
			response.getOptions().addOption(new Option(nr, contentType));
		} else {
			response.getOptions().addOption(new Option(nr, value));
		}
	}
	
	private void setResponseHeader(String header, int value)  {
		int nr = CoAPConstantsConverter.convertHeaderToInt(header);
		response.getOptions().addOption(new Option(nr, value));
	}
	
	private String getAllRequestHeaders() {
		final String nl = "\r\n";
		final String col = ": ";
		StringBuffer buffer = new StringBuffer();
		for (Option opt : request.getOptions().asSortedList()) {
			buffer.append(OptionNumberRegistry.toString(opt.getNumber()));
			buffer.append(col);
			buffer.append(opt.toString());
			buffer.append(nl);
		}
		return buffer.toString();
	}
	
//	private String CoapExchangeHeader(String header) {
//		int nr = CoAPConstantsConverter.convertHeaderToInt(header);
//		return CoapExchangeHeader(nr);
//	}
//	
//	private String CoapExchangeHeader(int nr) {
//		String col = ": ";
//		List<Option> opts = request.getOptions(nr);
//		return OptionNumberRegistry.toString(nr)+col+deflat(opts);
//	}
	
	private String deflat(List<Option> opts) {
		String sep = ", ";
		StringBuffer buffer = new StringBuffer();
		for (int i=0;i<opts.size();i++) {
			buffer.append(opts.get(i).toString());
			if (i<opts.size()-1)
				buffer.append(sep);
		}
		return buffer.toString();
	}
	
	private void respond(Object jscode, Object jsmessage, Object jscontentType) {
//		System.out.println("respond with jscode "+jscode+" ("+jscode.getClass()+"), " +
//		"jsmsg "+jscode+" ("+(jsmessage!=null?jscode.getClass():"-")+"), " +
//		"jsct "+jscontentType+" ("+(jscontentType!=null?jscontentType.getClass():"-")+")");

		Integer code;
		String message;
		Integer contentType;

		// Parse code (e.g. 69, 2.05 or "Content")
		if (jscode instanceof Integer)
			code = (Integer) jscode;
		else if (jscode instanceof String)
			code = CoAPConstantsConverter.convertStringToCode((String) jscode);
		else if (jscode instanceof Double)
			code = CoAPConstantsConverter.convertNumCodeToCode((Double) jscode);
		else if (jscode instanceof Float)
			code = CoAPConstantsConverter.convertNumCodeToCode(((Float) jscode).doubleValue());
		else
			throw new IllegalArgumentException( "JavaScriptCoAPRequest.respond expects a String, Integer or Double as first argument but got "+jscode);

		// Parse message
		if (jsmessage == null)
			message = "null";
		else if (jsmessage instanceof Undefined)
			message = null; // Either, no jsmessage has been provided or it was an undefined variable
		else
			message = jsmessage.toString();

		// Parse content type (e.g. "text/plain", 0 or nothing)
		if (jscontentType instanceof Integer)
			contentType = (Integer) jscontentType;
		else if (jscontentType instanceof String)
			contentType = CoAPConstantsConverter .convertStringToContentType((String) jscontentType);
		else
			contentType = null;

		// Respond to the request
		response = new Response(ResponseCode.valueOf(code));
		if (message != null)
			response.setPayload(message);
		if (contentType != null)
			response.getOptions().setContentFormat(contentType);
		exchange.respond(response);
	}
	
}

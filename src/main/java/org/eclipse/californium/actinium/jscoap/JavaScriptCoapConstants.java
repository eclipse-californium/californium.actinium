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

import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Bad_Gateway;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Bad_Option;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Bad_Request;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Changed;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Content;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Created;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Deleted;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Forbidden;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Gateway_Timeout;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Internal_Server_Error;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Method_Not_Allowed;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Not_Found;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Not_Implemented;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Precondition_Failed;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Proxying_Not_Supported;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Request_Entity_Too_Large;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Service_Unavailable;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Unauthorized;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Unsupported_Media_Type;
import static org.eclipse.californium.actinium.jscoap.JavaScriptCoapConstants.Valid;

import java.util.HashMap;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;

/**
 * CoAPConstants implements the most important constants from CoAP.
 */
public interface JavaScriptCoapConstants {
	
	// CodeRegistry constants as JS constants
	public static final int Created 			= ResponseCode.CREATED.value;
	public static final int Deleted 			= ResponseCode.DELETED.value;
	public static final int Valid 				= ResponseCode.VALID.value;
	public static final int Changed 			= ResponseCode.CHANGED.value;
	public static final int Content 			= ResponseCode.CONTENT.value;
	public static final int Bad_Request 		= ResponseCode.BAD_REQUEST.value;
	public static final int Unauthorized 		= ResponseCode.UNAUTHORIZED.value;
	public static final int Bad_Option 			= ResponseCode.BAD_OPTION.value;
	public static final int Forbidden 			= ResponseCode.FORBIDDEN.value;
	public static final int Not_Found 			= ResponseCode.NOT_FOUND.value;
	public static final int Method_Not_Allowed	= ResponseCode.METHOD_NOT_ALLOWED.value;
	public static final int Precondition_Failed = ResponseCode.PRECONDITION_FAILED.value;
	public static final int Request_Entity_Too_Large = ResponseCode.REQUEST_ENTITY_TOO_LARGE.value;
	public static final int Unsupported_Media_Type = ResponseCode.UNSUPPORTED_CONTENT_FORMAT.value;
	public static final int Internal_Server_Error = ResponseCode.INTERNAL_SERVER_ERROR.value;
	public static final int Not_Implemented 	= ResponseCode.NOT_IMPLEMENTED.value;
	public static final int Bad_Gateway 		= ResponseCode.BAD_GATEWAY.value;
	public static final int Service_Unavailable = ResponseCode.SERVICE_UNAVAILABLE.value;
	public static final int Gateway_Timeout 	= ResponseCode.GATEWAY_TIMEOUT.value;
	public static final int Proxying_Not_Supported = ResponseCode.PROXY_NOT_SUPPORTED.value;
}

class CoAPConstantsConverter {
	
	private static final HashMap<Double, Integer> numToCode = new HashMap<Double, Integer>();
	private static final HashMap<String, Integer> strToCode = new HashMap<String, Integer>();
	
	static {
		numToCode.put(2.01, Created);
		numToCode.put(2.02, Deleted);
		numToCode.put(2.03, Valid);
		numToCode.put(2.04, Changed);
		numToCode.put(2.05, Content);
		numToCode.put(4.00, Bad_Request);
		numToCode.put(4.01, Unauthorized);
		numToCode.put(4.02, Bad_Option);
		numToCode.put(4.03, Forbidden);
		numToCode.put(4.04, Not_Found);
		numToCode.put(4.05, Method_Not_Allowed);
		numToCode.put(4.12, Precondition_Failed);
		numToCode.put(4.13, Request_Entity_Too_Large);
		numToCode.put(4.15, Unsupported_Media_Type);
		numToCode.put(5.00, Internal_Server_Error);
		numToCode.put(5.01, Not_Implemented);
		numToCode.put(5.02, Bad_Gateway);
		numToCode.put(5.03, Service_Unavailable);
		numToCode.put(5.04, Gateway_Timeout);
		numToCode.put(5.05, Proxying_Not_Supported);
	}
	
	static {
		strToCode.put("Created", Created);
		strToCode.put("Deleted", Deleted);
		strToCode.put("Valid", Valid);
		strToCode.put("Changed", Changed);
		strToCode.put("Content", Content);
		strToCode.put("Bad Request", Bad_Request);
		strToCode.put("Unauthorized", Unauthorized);
		strToCode.put("Bad Option", Bad_Option);
		strToCode.put("Forbidden", Forbidden);
		strToCode.put("Not Found", Not_Found);
		strToCode.put("Method Not Allowed", Method_Not_Allowed);
		strToCode.put("Precondition Failed", Precondition_Failed);
		strToCode.put("Request Entity Too Large", Request_Entity_Too_Large);
		strToCode.put("Unsupported Media Type", Unsupported_Media_Type);
		strToCode.put("Internal Server Error", Internal_Server_Error);
		strToCode.put("Bad Gateway", Bad_Gateway);
		strToCode.put("Service Unavailable", Service_Unavailable);
		strToCode.put("Gateway Timeout", Gateway_Timeout);
		strToCode.put("Proxying Not Supported", Proxying_Not_Supported);
	}
	
	public static int convertNumCodeToCode(double num) {
		Integer ret = numToCode.get(num);
		if (ret!=null) return ret;
		else return Internal_Server_Error; // better throw an exception?
	}
	
	public static int convertStringToCode(String str) {
		Integer ret = strToCode.get(str);
		if (ret!=null) return ret;
		else return Internal_Server_Error; // better throw an exception?
	}
	
	public static int convertCoAPCodeToHttp(int code) {
		if (code==Created) return 201;
		else if (code==Deleted) return 204;
		else if (code==Valid) return 304;
		else if (code==Changed) return 204;
		else if (code==Content) return 200;
		else if (code==Bad_Request) return 400;
		else if (code==Unauthorized) return 401;
		else if (code==Bad_Option) return 400;
		else if (code==Forbidden) return 403;
		else if (code==Not_Found) return 404;
		else if (code==Method_Not_Allowed) return 405;
		else if (code==Precondition_Failed) return 412;
		else if (code==Request_Entity_Too_Large) return 413;
		else if (code==Unsupported_Media_Type) return 415;
		else if (code==Internal_Server_Error) return 500;
		else if (code==Not_Implemented) return 501;
		else if (code==Bad_Gateway) return 502;
		else if (code==Service_Unavailable) return 503;
		else if (code==Gateway_Timeout) return 504;
		else if (code==Proxying_Not_Supported) return 505;
		else return 0;
	}
	
	public static String convertCoAPCodeToString(int code) {
		return ResponseCode.valueOf(code).toString();
	}
}

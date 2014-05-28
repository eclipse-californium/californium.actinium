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

import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Accept;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Bad_Gateway;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Bad_Option;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Bad_Request;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Changed;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Content;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Content_Type;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Created;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Deleted;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.ETag;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Forbidden;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Gateway_Timeout;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.If_Match;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.If_None_Match;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Internal_Server_Error;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Location_Path;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Location_Query;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Max_Age;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Method_Not_Allowed;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Not_Found;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Not_Implemented;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Precondition_Failed;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Proxy_Uri;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Proxying_Not_Supported;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Request_Entity_Too_Large;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Service_Unavailable;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Token;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Unauthorized;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Unsupported_Media_Type;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Uri_Host;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Uri_Path;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Uri_Port;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Uri_Query;
import static org.eclipse.californium.actinium.jscoap.CoAPConstants.Valid;

import java.util.HashMap;

import ch.ethz.inf.vs.californium.coap.CodeRegistry;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.OptionNumberRegistry;

/**
 * CoAPConstants implements the most important constants from CoAP.
 */
public interface CoAPConstants {

	// Option Number Registry from draft-ietf-core-coap-18, chapter 12.2
	public static final String Content_Type 	= "Content-Type";
	public static final String Max_Age 			= "Max-Age";
	public static final String Proxy_Uri 		= "Proxy-Uri";
	public static final String ETag 			= "ETag";
	public static final String Uri_Host 		= "Uri-Host";
	public static final String Location_Path 	= "Location-Path";
	public static final String Uri_Port 		= "Uri-Port";
	public static final String Location_Query 	= "Location-Query";
	public static final String Uri_Path 		= "Uri-Path";
	public static final String Token 			= "Token";
	public static final String Accept 			= "Accept";
	public static final String If_Match 		= "If-Match";
	public static final String Uri_Query 		= "Uri-Query";
	public static final String If_None_Match 	= "If-None-Match";
	
	// CodeRegistry constants as JS constants
	public static final int Created 			= CodeRegistry.RESP_CREATED;
	public static final int Deleted 			= CodeRegistry.RESP_DELETED;
	public static final int Valid 				= CodeRegistry.RESP_VALID;
	public static final int Changed 			= CodeRegistry.RESP_CHANGED;
	public static final int Content 			= CodeRegistry.RESP_CONTENT;
	public static final int Bad_Request 		= CodeRegistry.RESP_BAD_REQUEST;
	public static final int Unauthorized 		= CodeRegistry.RESP_UNAUTHORIZED;
	public static final int Bad_Option 			= CodeRegistry.RESP_BAD_OPTION;
	public static final int Forbidden 			= CodeRegistry.RESP_FORBIDDEN;
	public static final int Not_Found 			= CodeRegistry.RESP_NOT_FOUND;
	public static final int Method_Not_Allowed	= CodeRegistry.RESP_METHOD_NOT_ALLOWED;
	public static final int Precondition_Failed = CodeRegistry.RESP_PRECONDITION_FAILED;
	public static final int Request_Entity_Too_Large = CodeRegistry.RESP_REQUEST_ENTITY_TOO_LARGE;
	public static final int Unsupported_Media_Type = CodeRegistry.RESP_UNSUPPORTED_MEDIA_TYPE;
	public static final int Internal_Server_Error = CodeRegistry.RESP_INTERNAL_SERVER_ERROR;
	public static final int Not_Implemented 	= CodeRegistry.RESP_NOT_IMPLEMENTED;
	public static final int Bad_Gateway 		= CodeRegistry.RESP_BAD_GATEWAY;
	public static final int Service_Unavailable = CodeRegistry.RESP_SERVICE_UNAVAILABLE;
	public static final int Gateway_Timeout 	= CodeRegistry.RESP_GATEWAY_TIMEOUT;
	public static final int Proxying_Not_Supported = CodeRegistry.RESP_PROXYING_NOT_SUPPORTED;
}

class CoAPConstantsConverter {
	
	private static final HashMap<Double, Integer> numToCode = new HashMap<Double, Integer>();
	private static final HashMap<String, Integer> strToCode = new HashMap<String, Integer>();
	private static final HashMap<String, Integer> strToContentType = new HashMap<String, Integer>();
	
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
	
	// compare to ch.ethz.inf.vs.californium.coap.MediaTypeRegistry
	static {
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.UNDEFINED), MediaTypeRegistry.UNDEFINED);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_PLAIN), MediaTypeRegistry.TEXT_PLAIN);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_CSV), MediaTypeRegistry.TEXT_CSV);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.TEXT_HTML), MediaTypeRegistry.TEXT_HTML);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.IMAGE_GIF), MediaTypeRegistry.IMAGE_GIF);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.IMAGE_JPEG), MediaTypeRegistry.IMAGE_JPEG);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.IMAGE_PNG), MediaTypeRegistry.IMAGE_PNG);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.IMAGE_TIFF), MediaTypeRegistry.IMAGE_TIFF);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.APPLICATION_LINK_FORMAT), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.APPLICATION_XML), MediaTypeRegistry.APPLICATION_XML);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.APPLICATION_OCTET_STREAM), MediaTypeRegistry.APPLICATION_OCTET_STREAM);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.APPLICATION_RDF_XML), MediaTypeRegistry.APPLICATION_RDF_XML);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.APPLICATION_SOAP_XML), MediaTypeRegistry.APPLICATION_SOAP_XML);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.APPLICATION_ATOM_XML), MediaTypeRegistry.APPLICATION_ATOM_XML);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.APPLICATION_XMPP_XML), MediaTypeRegistry.APPLICATION_XMPP_XML);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.APPLICATION_EXI), MediaTypeRegistry.APPLICATION_EXI);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.APPLICATION_FASTINFOSET), MediaTypeRegistry.APPLICATION_FASTINFOSET);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.APPLICATION_SOAP_FASTINFOSET), MediaTypeRegistry.APPLICATION_SOAP_FASTINFOSET);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.APPLICATION_JSON), MediaTypeRegistry.APPLICATION_JSON);
		strToContentType.put(MediaTypeRegistry.toString(MediaTypeRegistry.APPLICATION_X_OBIX_BINARY), MediaTypeRegistry.APPLICATION_X_OBIX_BINARY);
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
	
	public static int convertStringToContentType(String str) {
		Integer ret = strToContentType.get(str);
		if (ret!=null) return ret;
		else return MediaTypeRegistry.UNDEFINED; // better throw an exception?
	}
	
	public static int convertHeaderToInt(String c) {
		if (Content_Type.equals(c)) 	return OptionNumberRegistry.CONTENT_TYPE;
		else if (Max_Age.equals(c)) 	return OptionNumberRegistry.MAX_AGE;
		else if (Proxy_Uri.equals(c)) 	return OptionNumberRegistry.PROXY_URI;
		else if (ETag.equals(c)) 		return OptionNumberRegistry.ETAG;
		else if (Uri_Host.equals(c))	return OptionNumberRegistry.URI_HOST;
		else if (Location_Path.equals(c))return OptionNumberRegistry.LOCATION_PATH;
		else if (Uri_Port.equals(c))	return OptionNumberRegistry.URI_PORT;
		else if (Location_Query.equals(c))return OptionNumberRegistry.LOCATION_QUERY;
		else if (Uri_Path.equals(c))	return OptionNumberRegistry.URI_PATH;
		else if (Token.equals(c))		return OptionNumberRegistry.TOKEN;
		else if (Accept.equals(c))		return OptionNumberRegistry.ACCEPT;
		else if (If_Match.equals(c))	return OptionNumberRegistry.IF_MATCH;
		else if (Uri_Query.equals(c))	return OptionNumberRegistry.URI_QUERY;
		else if (If_None_Match.equals(c)) return OptionNumberRegistry.IF_NONE_MATCH;
		else throw new IllegalArgumentException("Unknown Header: "+c);
	}
	
	public static String convertIntToHeader(int nr) {
		if (nr==1) return Content_Type;
		else if (nr==2) return Max_Age;
		else if (nr==3) return Proxy_Uri;
		else if (nr==4) return ETag;
		else if (nr==5) return Uri_Host;
		else if (nr==6) return Location_Path;
		else if (nr==7) return Uri_Port;
		else if (nr==8) return Location_Query;
		else if (nr==9) return Uri_Path;
		else if (nr==11) return Token;
		else if (nr==12) return Accept;
		else if (nr==13) return If_Match;
		else if (nr==15) return Uri_Query;
		else return "Unknown";
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
		return CodeRegistry.toString(code);
	}
}

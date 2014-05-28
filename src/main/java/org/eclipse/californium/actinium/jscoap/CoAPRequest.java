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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Function;

import ch.ethz.inf.vs.californium.coap.DELETERequest;
import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.Option;
import ch.ethz.inf.vs.californium.coap.OptionNumberRegistry;
import ch.ethz.inf.vs.californium.coap.POSTRequest;
import ch.ethz.inf.vs.californium.coap.PUTRequest;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.Message.messageType;

/**
 * CoAPRequest implements the CoAPRequest API
 * (http://lantersoft.ch/download/bachelorthesis/CoAPRequest_API.pdf).
 * <p>
 * The CoAPRequest specification defines an API that provides scripted client
 * functionality for transferring data between a CoAP client and a CoAP server.
 * <p>
 * A simple code demonstration to fetch data from a CoAP resource over the network
 * <pre>
 * var client = new CoAPRequest();
 * client.onreadystatechange = handler
 * client.open("GET", "coap://mydomain.myresource");
 * client.send();
 * function handler() {
 *   if (client.readyState==4) {
 *     if (client.status==client.Content) {
 *       processData(client.responseText);
 *     }
 *   }
 * }
 * </pre>
 */
public class CoAPRequest implements CoAPConstants {

	public static int UNSENT = 0;
	public static int OPENED = 1;
	public static int HEADERS_RECEIVED = 2; // not used, since receiving happens wihtin Californium framework
	public static int LOADING = 3; // not used, since loading happens wihtin Californium framework
	public static int DONE = 4;
	 
	// response
	public Response response; // the whole response
	public String responseType = ""; // the type of the response (e.g. "text/plain")
	public String responseText; // the payload as string
	public String responseLocationPath; // the response's location header (if defined)
	
	public Function onreadystatechange; 
	public Function ontimeout; // called, if timeout occurs
	public Function onload; // called, if response successfully received
	public Function onerror; // called, if a network error in Californium occurs
	
	// request
	public String uri; // target of the request
	public boolean async; // true, if request shall be sent asynchronously
	public String method; // "GET", "POST", "PUT" or "DELETE"
	public String locationPath; // request's location path
	public long timeout; // milliseconds (timeout==0 ==> no timeout)
	public Integer contenttype; // contentType as integer (e.g. application/xml == 41)
	public boolean confirmable;
	
	// status
	public volatile boolean send; // true, if request has been sent
	public volatile boolean error; // true, if error has occurred

	public volatile int readyState; // 0,1,4
	public volatile int httpstatus; // HTTP status
	public volatile int status; // CoAP status
	public volatile String statusText; // CoAP status as string
	
	// internal
	public Map<Integer, List<Option>> options = new HashMap<Integer, List<Option>>();

	// sender, that has sent the last request
	private Sender sender;
	
	/**
	 * Open connection to specified URI. Resets state from last request.
	 * @param method "GET", "POST", "PUT" or "DELETE"
	 * @param uri the URI
	 */
	public void open(String method, String uri) {
		open(method, uri, true);
	}

	/**
	 * Open connection to specified URI. Creates request. Resets state from last
	 * request.
	 * 
	 * @param method "GET", "POST", "PUT" or "DELETE"
	 * @param uri the URI
	 * @param async true, if asynchronous request
	 */
	public synchronized void open(String method, String uri, boolean async) {
		open(method,uri,async,true);
	}
	

	public synchronized void open(String method, String uri, boolean async, boolean confirmable) {
		if (!isValidMethod(method))
			throw new IllegalArgumentException("Invalid method "+method+". Only GET, POST, PUT and DELETE allowed");

		if (!isValidURI(uri))
			throw new IllegalArgumentException("Invalid URI "+uri);
		
		// terminate abort: do nothing
		// terminate send:
		abort();
		
		this.uri = uri;
		this.method = method;
		this.async = async;
		this.send = false;
		this.confirmable = confirmable;
		setResponse(null);
		
		setReadyState(OPENED);
		AbstractSender.callJavaScriptFunction(onreadystatechange, this);
	}

	/**
	 * Aborts the last request.
	 */
	public void abort() {
		if (sender!=null)
			sender.abort();
	}
	
	/**
	 * Send an empty message.
	 */
	public void send() {
		send("");
	}

	/**
	 * Send the specified data.
	 * @param data data to send
	 * @throws InterruptedException 
	 */
	public void send(String data) {
		
		Sender s; Request request;
		synchronized(this) {

			// verify valid status
			if (readyState!=OPENED)
				throw new IllegalStateException("state must be OPENED (==2) but is "+readyState);
			
			if (send)
				throw new IllegalStateException("The flag send must not be set when calling send() (first call open())");
			
			this.error = false;
			this.send = true;
			
			// create request
			request = createNewRequest(data);
			
			// create sender
			if (isAsync()) {
				s = new AsynchronousSender(this, onreadystatechange, ontimeout, onload, onerror, timeout);
			} else {
				s = new SynchronousSender(this, onreadystatechange, ontimeout, onload, onerror, timeout);
			}
			this.sender = s;
			
		}
		// send request
		// Must not be within synchronized-block, if request not async.
		s.send(request);
	}
	
	/**
	 * Sets the specified locationPath to the request
	 * @param locationPath the locationPath
	 */
	public void setLocationPath(String locationPath) {
		this.locationPath = locationPath;
	}
	
	/**
	 * Returns the request's locationPath
	 * @return the request's locationPath
	 */
	public String getLocationPath() {
		return locationPath;
	}
	
	/**
	 * Sets the option for observing the target resource.
	 */
	public void setObserverOption() {
		setObserverOption("");
	}
	
	/**
	 * Sets the option for observing the target resource.
	 * @param str string for option
	 */
	public void setObserverOption(String str) {
		addOption(str, OptionNumberRegistry.OBSERVE);
	}
	
	/**
	 * Removes the observing option.
	 */
	public void removeObserverOption() {
		removeOptions(OptionNumberRegistry.OBSERVE);
	}
	
	/**
	 * Adds the specified option.
	 * @param nr the option nr
	 */
	public void addOption(int nr) {
		List<Option> list = findOptionList(nr);
		list.add(new Option(nr));
	}
	
	/**
	 * Adds the specified option.
	 * @param val the option's value.
	 * @param nr the option nr.
	 */
	public void addOption(int val, int nr) {
		List<Option> list = findOptionList(nr);
		list.add(new Option(val, nr));
	}
	
	/**
	 * Adds the specified option.
	 * @param str the option's text.
	 * @param nr the option nr.
	 */
	public void addOption(String str, int nr) {
		List<Option> list = findOptionList(nr);
		list.add(new Option(str, nr));
	}
	
	/**
	 * Finds the list of options for the specified nr or creates a new one.
	 */
	private List<Option> findOptionList(Integer nr) {
		List<Option> list = options.get(nr);
		if (list==null) {
			list = new LinkedList<Option>();
			options.put(nr, list);
		}
		return list;
	}
	
	/**
	 * Removes the specified options.
	 * @param nr the option nr.
	 */
	public void removeOptions(int nr) {
		options.remove(nr);
	}
	
	/**
	 * Removes all options.
	 */
	public void clearOptions() {
		options.clear();
	}
	
	/**
	 * Sets the specified contentType.
	 * @param ct the contentType.
	 */
	public void setContentType(int ct) {
		contenttype = ct;
	}
	
	/**
	 * Removes teh contentType.
	 */
	public void removeContentType() {
		contenttype = null;
	}
	
	/**
	 * Adds the specified request header.
	 * @param header the header.
	 * @param value the text.
	 */
	public void setRequestHeader(String header, String value)  {
		checkOpenUnsentState();
		
		int nr = CoAPConstantsConverter.convertHeaderToInt(header);
		if (nr==OptionNumberRegistry.CONTENT_TYPE) {
			// we also have to parse the value to get it as integer
			int contentType = CoAPConstantsConverter.convertStringToContentType(value);
			addOption(contentType,nr);
		} else if (nr==OptionNumberRegistry.ACCEPT) {
			// we also have to parse the value to get it as integer
			int contentType = CoAPConstantsConverter.convertStringToContentType(value);
			addOption(contentType,nr);
		} else {
			addOption(value, nr);
		}
	}
	
	/**
	 * Adds the specified request header.
	 * @param header the header.
	 * @param value the value.
	 */
	public void setRequestHeader(String header, int value)  {
		checkOpenUnsentState();
		
		int nr = CoAPConstantsConverter.convertHeaderToInt(header);
		addOption(value, nr);
	}

	/**
	 * Check, whether the readyState is OPEN and the request has not been sent
	 * yet. If not, throw an IllegalStateException.
	 */
	private void checkOpenUnsentState() {
		if (readyState!=OPENED)
			throw new IllegalStateException("state must be OPENED (==2) but is "+readyState);
		if (send)
			throw new IllegalStateException("The flag send must not be set when calling send() (first call open())");
	}
	
	/**
	 * Returns a String containing all headers. 
	 * @return a String containing all headers.
	 */
	public String getAllResponseHeaders() {
		if (readyState==UNSENT || readyState==OPENED)
			return "";
		if (error) return "";
		if (response==null) return "";
		final String nl = "\r\n";
		final String col = ": ";
		StringBuffer buffer = new StringBuffer();
		/*
		Map<Integer, List<Option>> map = response.getOptionMap();
		Set<Integer> set = map.keySet();
		Integer[] keys = set.toArray(new Integer[set.size()]);
		for (int j=0;j<keys.length;j++) {
			Integer nr = keys[j];
			buffer.append(getResponseHeader(nr));
			if (j<keys.length-1)
				buffer.append(nl);
		}
		*/
		for (Option opt : response.getOptions()) {
			buffer.append(OptionNumberRegistry.toString(opt.getOptionNumber()));
			buffer.append(col);
			buffer.append(opt.toString());
			buffer.append(nl);
		}
		return buffer.toString();
	}
	
	/**
	 * Returns the specified header.
	 * @param header the header name.
	 * @return the header values
	 */
	public String getResponseHeader(String header) {
		if (readyState==UNSENT || readyState==OPENED)
			return "";
		if (error) return "";
		if (response==null) return "";
		int nr = CoAPConstantsConverter.convertHeaderToInt(header);
		return getResponseHeader(nr);
	}
	
	/**
	 * Returns the header for the specified header nr.
	 * @param nr the nr.
	 * @return the corresponding header.
	 */
	private String getResponseHeader(int nr) {
		String col = ": ";
		List<Option> opts = response.getOptions(nr);
		return OptionNumberRegistry.toString(nr)+col+deflat(opts);
	}
	
	/**
	 * Converts the specified list of options into a string.
	 * @param opts the list of options
	 * @return the string.
	 */
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

	/**
	 * Returns true, if the request should be sent asynchronously.
	 * @return true, if the request should be sent asynchronously.
	 */
	public boolean isAsync() {
		return async;
	}

	/**
	 * Sets the flag, whether the request should be sent asynchronously.
	 * @param async a flag.
	 */
	public void setAsync(boolean async) {
		this.async = async;
	}
	
	// called by Sender
	/**
	 * Sets the specified response or resets the response, if response is null.
	 */
	protected synchronized void setResponse(Response response) {
		if (response!=null) {
			this.response = response;
			this.responseText = response.getPayloadString();
			this.responseType = MediaTypeRegistry.toString(response.getContentType());
			this.responseLocationPath = response.getLocationPath();
			this.status = response.getCode();
			this.statusText = CoAPConstantsConverter.convertCoAPCodeToString(this.status);
			this.httpstatus = CoAPConstantsConverter.convertCoAPCodeToHttp(status);
			
		} else {
			this.response = null;
			this.responseText = null;
			this.responseType = "";
			this.responseLocationPath = null;
			this.status = 0;
			this.statusText = "";
			this.httpstatus = 0;
		}
	}
	
	/**
	 * Sets the readyState.
	 * @param readyState the readyState.
	 */
	protected synchronized void setReadyState(int readyState) {
		this.readyState = readyState;
	}
	
	/**
	 * Sets the error flag.
	 * @param flag the flag.
	 */
	protected synchronized void setError(boolean flag) {
		this.error = flag;
	}
	
	/**
	 * Sets the send flag.
	 * @param flag the flag.
	 */
	protected synchronized void setSend(boolean flag) {
		this.send = flag;
	}
	
	/**
	 * Creates a new request with the specified data as payload.
	 * @param data the payload.
	 * @return the request.
	 */
	private Request createNewRequest(String data) {
		Request request;
		
		if (CoAPMethod.GET.equals(method)) {
			request = new GETRequest();
		
		} else if (CoAPMethod.POST.equals(method)) {
			request = new POSTRequest();
		
		} else if (CoAPMethod.PUT.equals(method)) {
			request = new PUTRequest();
		
		} else if (CoAPMethod.DELETE.equals(method)) {
			request = new DELETERequest();
		
		} else {
			throw new IllegalArgumentException("Unknown CoAP method: "+method+". Only \"GET\", \"POST\", \"PUT\" and \"DELETE\" are allowed");
		}
		
		request.setType(confirmable ? messageType.CON : messageType.NON);
		request.setURI(uri);
		request.setPayload(data);
		
		/*
		 * set options, create a new list with the options, so that the request
		 * is independent on the lists and the hashmap options
		 */
		for (Integer i:options.keySet()) {
			List<Option> list = options.get(i);
			request.setOptions(new LinkedList<Option>(list));
		}
		
		if (locationPath!=null)
			request.setLocationPath(locationPath);
		
		if (contenttype!=null)
			request.setContentType(contenttype.intValue());
		
		return request;
	}
	
	/**
	 * Returns true, if the specified URI is typed correctly.
	 * @param uri the URI.
	 * @return true, if the specified URI is typed correctly.
	 */
	private boolean isValidURI(String uri) {
		// let class util.net.URI check
		try {
			if (uri!=null) {
				new URI(uri);
				return true;
			} else {
				return false;
			}
		} catch (URISyntaxException e) {
			return false;
		}
	}
	
	/**
	 * Returns true, if the specified method is typed correctly.
	 * @param method the method.
	 * @return true, if the specified method is typed correctly.
	 */
	private boolean isValidMethod(String method) {
		return "GET".equals(method) ||
				"POST".equals(method) ||
				"PUT".equals(method) ||
				"DELETE".equals(method);
	}
}

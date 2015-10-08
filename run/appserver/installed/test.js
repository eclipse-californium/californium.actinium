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
 *    Martin Lanter
 ******************************************************************************/
/*
 * Communicator tests the CoapRequest API.
 *
 * Communicator contains 6 subresources to adjust the CoapRequest
 *   async: true, if reqeust should be sent asynchronously.
 *          false, if request should be sent synchronously.
 *   contentType: "null" for undefined
 *                An integer, e.g. application/xml==41
 *   locationPath: A location path of the request
 *   method: GET, POST, PUT or DELETE
 *   timeout: 0, if no timeout (for CoapRequest) should be used
 *            An integer, to define a timeout (for (CoapRequest)
 *   uri: A URI as target for the request
 *
 * Send a PUT request with the desired value as payload to change
 * the value of one of the 6 subresources above.
 *
 * The subresource send initiates the sending mechanism.
 * Send a POST request with the desired payload as payload (makes sense)
 * to send the specified payload to the target URI, using the
 * values from the 6 subresources above.
 *
 * In short: Specify the values of a CoapRequest with subresources
 * from above and then send a POST to send to send the CoapRequest.
 */
 
 
 app.dump("huhu");
 
var target = app.getProperty("target");
var isasync = app.getProperty("send_async","true");

var uri = new Property("uri",target);
var async = new Property("async",isasync);
var method = new Property("method","POST");
var locationPath = new Property("locationPath","");
var contentType = new Property("contentType", "null");
var timeout = new Property("timeout", "1000");
var send = new Send();

app.root.add(uri.res);
app.root.add(method.res);
app.root.add(async.res);
app.root.add(locationPath.res);
app.root.add(contentType.res);
app.root.add(timeout.res);
app.root.add(send.res);

app.root.onget = function(request) {
	performRequest("GET", request);
}

app.root.onpost = function(request) {
	performRequest("POST", request);
}

app.root.onput = function(request) {
	performRequest("PUT", request);
}

app.root.ondelete = function(request) {
	performRequest("DELETE", request);
}

/*
 * The communicator app responds that the request has been received.
 */
function performRequest(type, request) {
	app.dump("----");
	app.dump("Received "+type+"Request");
	app.dump("Payload: "+request.requestText);
	app.dump("LocationPath: "+request.getLocationPath());
	
	// compute result
	app.sleep(100);
	
	request.respond(2.05, app.root.getName()+" received your message \""+request.requestText+"\"");
}

function Property(resid, dflt) {
	var THIS = this;
	this.content = dflt;
	this.res = new JavaScriptResource(resid);
	this.res.onget = function(request) {
		request.respond(2.05, THIS.content);
	};
	this.res.onput = function(request) {
		THIS.content = request.requestText;
		request.respond(2.05, THIS.content);
	};
}

/*
 * The send subresource sends a synchronous or asynchronous 
 * request to the specified uri. It uses a CoapRequest object 
 * with the specified values from the other subresources
 */
function Send() {
	var THIS = this;
	this.res = new JavaScriptResource("send");
	this.res.onpost = function(request) {
		dosend(request);
	}
}

function dosend(request) {
	if (async.content=="true") {
		dosendAsync(request);
	} else {
		dosendSync(request);
	}
}

function dosendAsync(request) {
	try {
		app.dump("\nsend assynchronous request to "+uri.content);
		request.accept(); // accept request and respond later
		var payload = request.requestText;

		// create new coaprequest to call uri
		var coapreq = new CoapRequest();
		coapreq.onreadystatechange = function() {
			app.dump("onreadystatechange called while sending asynchronously, readyState: "+this.readyState+", error="+this.error);
		};
		coapreq.open(method.content, uri.content, true);
		coapreq.locationPath = locationPath.content;
		coapreq.timeout = Long.parseLong(timeout.content); 
		if (contentType.content!="null") {
			coapreq.contentType = parseInt(contentType.content);
		}
		
		coapreq.onload = function(response) {
			app.dump("----");
			app.dump("Received asynchronous response");
			app.dump("Status: "+coapreq.statusString+" (http: "+coapreq.httpstatus+")");
			app.dump("Payload: "+coapreq.responseText);
			app.dump("LocationPath: "+coapreq.responseLocationPath);
			app.dump("ContentType: "+coapreq.responseType);
			app.dump("Headers:\n"+coapreq.getAllResponseHeaders());

			// respond the request
			request.respond(2.05, "request sent asyncronously: response = "+this.responseText);
		}
				
		coapreq.ontimeout = function() {
			app.dump("timeout while sending asynchronously");
			request.respond(2.05, "timeout while sending asynchronously");
		}
		
		coapreq.setRequestHeader("Max-Age",77);
		coapreq.send(payload);

	} catch (e if e.javaException instanceof Exception) {
		e.javaException.printStackTrace();
		request.respond(4.00, e.javaException.toString());
	}
}

function dosendSync(request) {
	try {
		app.dump("\nsend synchronous request to "+uri.content);
		request.accept(); // accept request and respond later
		var payload = request.requestText;

		var coapreq = new CoapRequest();
		coapreq.ontimeout = ontimeoutSync;
		coapreq.onreadystatechange = function() {
			app.dump("onreadystatechange called while sending synchronously, readyState: "+this.readyState+", error="+this.error);
		};

		coapreq.open(method.content, uri.content, false);
		coapreq.locationPath = locationPath.content;
		coapreq.timeout = Long.parseLong(timeout.content); 
		
		if (contentType.content!="null") {
			coapreq.contentType = parseInt(contentType.content);
			app.dump("coapreq wird mit ct "+coapreq.contentType+" geschickt");
		}
		
		coapreq.send(payload);

		app.dump("----");
		app.dump("Received synchronous response");
		app.dump("Status: "+coapreq.statusString+" (http: "+coapreq.httpstatus+")");
		app.dump("Payload: "+coapreq.responseText);
		app.dump("LocationPath: "+coapreq.responseLocationPath);
		app.dump("ContentType: "+coapreq.responseType);
		app.dump("Headers:\n"+coapreq.getAllResponseHeaders());

		request.respond(coapreq.Content, "request sent syncronously: response = "+coapreq.responseText);
	
	} catch (e if e.javaException instanceof RequestErrorException) {
		request.respond(coapreq.Bad_Request, "Request error occured: "+e.javaException.toString());
	}
}

function ontimeoutSync() {
	app.dump("ontimeout called while sending synchronously");

}

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

var child = new JavaScriptResource("child");
child.onpost = function(request) {
	app.dump("request text: "+request.requestText);
	app.dump("request headers: "+request.getAllRequestHeaders());
	
	request.setResponseHeader("Max-Age", 55);
	request.setResponseHeader("Content-Type", "text/plain");
	request.setLocationPath("my/location/path");
	request.respond(2.05, "response blabla", 3);
}

app.root.add(child);

var req = new CoapRequest();
req.open("POST", "coap://localhost:5683/apps/running/self-test/child", true); // asynchronous
req.setRequestHeader("Accept", "application/json");
req.setRequestHeader("Max-Age", 77);
req.setRequestHeader("Uri-Host", "localhost");
req.onload = function() {
	app.dump(this.response);
	
	app.dump("response text: "+this.responseText);
	app.dump("response headers: "+this.getAllResponseHeaders());
}

req.send("payload blabla");

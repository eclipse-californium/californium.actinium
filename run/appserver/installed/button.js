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
 *    Matthias Kovatsch
 ******************************************************************************/
var request = new CoAPRequest();

app.onunload = onunload;


// remove Observer
function onunload() {
	var request = new CoAPRequest();
	app.dump("onunload: unregister from econotag1");
	request.open("GET", "coap://econotag2.local/sensors/button", true);
	request.onreadystatechange = null;
	request.send();
}

function handleNotification() {
	var request = new CoAPRequest();
	app.dump("Button update: " + this.responseText);
	request.open("POST", "coap://econotag1.local/actuators/toggle", true, false);
	request.onload = null;
	request.send();
}


request.open("GET", "coap://econotag2.local/sensors/button", true);
request.setObserverOption();
request.onload = handleNotification;
request.send();

app.dump("subscribed");

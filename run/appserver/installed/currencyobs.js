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
 * This app uses a timer to get notified every 2000 ms. All 2 seconds it calls
 * the XE currency web service and retrieves the current currency exchange rate
 * between Euros (EUR) and Swiss Francs (CHF). On a GET request the app returns
 * the history of values retrieved. This app is supposed to test how to register
 * observers with JavaScript apps on other JavaScript apps.
 */

importPackage(Packages.javax.xml.parsers);
importPackage(Packages.org.w3c.dom);
importPackage(Packages.org.xml.sax);

// TODO: How to find californium host?
var HOST = "coap://localhost:5683/";
var TIMER_RES = "apps/running/timer";
var FREQUENCY = "2000";

var SOURCE = "EUR";
var TARGET = "CHF";

var values = new Array();

// wait until all apps (incl. timer) have started up
java.lang.Thread.sleep(100);

// create new timer resource
var request = new CoapRequest();

request.open('POST', HOST+TIMER_RES, false); // new sync Request
request.send("curobstimer"); // new timer's name
app.dump(request.responseText);

// set timer's periode to 2000 ms
var timerpath = request.responseLocationPath;
request = new CoapRequest();
request.open("POST", HOST+timerpath, true); // new async Request
request.send(FREQUENCY); // timer's new periode

// register observing GET request (with same coaprequest)
request.open("GET", HOST+timerpath, true);
request.setObserverOption();
request.onload = callXECurrency;
request.send();

app.onunload = onunload;

app.root.onget = function(request) {
	var buffer = new StringBuffer("EUR - CHF currency exchange rate\n");
	for (var i=0;i<values.length;i++) {
		buffer.append(values[i]+"\n");
	}
	request.respond(2.05, buffer.toString());
};

app.root.onpost = function(request) {
	var payload = request.requestText;
	if (payload=="stop") {
		app.shutdown();
		request.respond(2.03);
	} else if (payload=="restart") {
		app.restart();
		request.respond("Valid");
	} else {		
		request.respond("Bad Request");
	}
};

// remove Observer
function onunload() {
	app.dump("onunload: unregister from "+HOST+timerpath);
	request.open('PUT', HOST+timerpath);
	request.onreadystatechange = null;
	request.send();
}

function callXECurrency() {
	var data = "FromCurrency="+SOURCE+"&ToCurrency="+TARGET;

	var xhr = new XMLHttpRequest();
	xhr.open("GET", "http://www.webservicex.net/CurrencyConvertor.asmx/ConversionRate", false);
	xhr.send(data);	
	
	// xhr.responseText looks like
	// <?xml version="1.0" encoding="utf-8"?><double xmlns="http://www.webserviceX.NET/">1.2071</double>
	
	// we have to remove the <?xml ...?> term because rhino doesn't support it
	// see https://developer.mozilla.org/en/E4X
	var text = xhr.responseText.replace(/^<\?xml\s+version\s*=\s*(["'])[^\1]+\1[^?]*\?>/, "").trim(); //"
	var xml = new XML(text);
	
	var value = xml.*;
	values[values.length] = value;
}

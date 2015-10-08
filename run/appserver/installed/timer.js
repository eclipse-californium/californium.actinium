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
 * This app is a timer. You can create your own timer by sending a POST request
 * to this app with the name as payload. A new timer is created and added as
 * subresource, changing every 1000 ms. Send a POST request with another number
 * as payload to it to change the timer's periode. Send a GET request to a timer
 * with the observe option enabled to register an observer. After every periode
 * (1000 ms as default) a timer calls changed() and a notification is sent to
 * all registered resources. This app is supposed to serve as an observable
 * resource to test how other JavaScript resources are able to register themself
 * to a JavaScript app.
 */

var timers = new Array(); // all names of timers
var paths = new Array(); // all names of timers

// create a new timer
function Timer(millis, name) {
	periode = millis;
	var mythis = this;
	this.timerres = new JavaScriptResource(name);
	this.timerres.onget = function(request) {
		request.respond(2.05, "timer periode: "+periode);
	};
	this.timerres.onpost = function(request) {
		try {
			periode = parseInt(request.requestText);
			request.respond(2.04, "changed timer periode to "+periode);
		} catch (e if e.javaException instanceof java.lang.NumberFormatException) {
			request.respond(4.00, e.javaException.getMessage());
		}
	};
	/**
	 * Start new timer in a new thread.
	 */
	this.go = function() {
		thread = Thread(function() {
			while(true) {
				for (var i=0;i<periode/1000;i++) {
					java.lang.Thread.sleep(1000);
				}
				java.lang.Thread.sleep(periode%1000);
				mythis.timerres.changed();
			}
		});
		thread.start();
	};
	this.timerres.setObservable(true);
	app.root.add(this.timerres);
	this.go();
}

app.root.onget = function(exchange) {
	var buffer = new java.lang.StringBuffer();
	for (var i=0;i<timers.length;i++) {
		buffer.append(timers[i]+"\n");
	}
	exchange.respond(2.05, buffer.toString());
}

app.root.onpost = function(exchange) {
	var name = exchange.requestText;
	app.dump(name);
	if (!isInUse(name)) {
		var timer = new Timer(1000,name);
		var path = timer.timerres.getURI();
		timers[timers.length] = name;
		paths[paths.length] = path;
		
		exchange.setLocationPath(path);
		exchange.respond(2.01, "Timer "+name+" created at location "+path);
	} else {
		exchange.setLocationPath(findPathForName(name));
		exchange.respond(4.00, "Timer name "+name +" is already in use");
	}
}

function isInUse(name) {
	for (var i=0;i<timers.length;i++) {
		if (timers[i] == name) {
			return true;
		}
	}
	return false;
}

function findPathForName(name) {
	for (var i=0;i<timers.length;i++) {
		if (timers[i] == name) {
			return paths[i];
		}
	}
	return null;
}

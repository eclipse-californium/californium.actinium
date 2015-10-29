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
 * This app tests the bahavior of another resource in the event of a delay.
 * Send a POST request with payload [accept][time] (payload can be empty).
 *   accept: accept the request (request.accept())
 *   time: time to wait before response
 * Examples:
 *   ""         = no accept, no response
 *   "accept"   = accept, no response
 *   "accept77" = accept, response after 77 ms
 *   "500"      = no accept, response after 500 ms
 */

if (typeof String.prototype.startsWith != 'function') {
  String.prototype.startsWith = function (str){
    return this.indexOf(str) == 0;
  };
}


var counter = 0;

app.root.onget = function(request) {
	request.respond(2.05, howto);
}

function respond(request) {
	var payload = request.requestText;
	var timestr;
	
	if (payload.startsWith("accept")) {
		app.dump("accept request "+request.getMID());
		request.accept();
		
		timestr = payload.substring(6);
	} else {
		timestr = payload;
	}
	
	if (timestr !== "") {
		var time = getTime(timestr);
		
		if (time<0) {
			app.dump("Invalid time in request "+request.getMID());
			request.respond(2.05, "Invalid time "+timestr+" at ("+counter+")");
		} else {
			app.dump("Wait for "+time+" ms to respond to reqeust "+request.getMID());
			app.sleep(time);
			app.dump("Respond ("+counter+") to reqeust "+request.getMID());
			//app.dump("This request's type is "+request.getType());
			request.respond(2.05, "Response ("+counter+") after "+time+" ms");
		}
	} else {
		app.dump("No response ("+counter+") to request"+request.getMID());
	}
	
	counter++;
}


app.root.onpost = respond;
app.root.onput = respond;
app.root.ondelete = respond;

function hasAccept(payload) {
	return payload.startsWith("accept");
}

function getTime(str) {
	try {
		var time = parseInt(str);
		return time;
	} catch (e if e.javaException instanceof NumberFormatException) {				
		return -1;
	}
}

var howto = "Send a POST request with payload: " +
	"\n[accept][time]" +
	"\n\taccept: accepts message (request.accept())" +
	"\n\ttime: time to wait before response" +
	"\nExpamples:" + 
	"\n\t\"\" = no accept, no response" +
	"\n\t\"accept\" = accept, no response" +
	"\n\t\"accept77\" = accept, response after 77 ms" +
	"\n\t\"500\" = no accept, response after 500 ms";
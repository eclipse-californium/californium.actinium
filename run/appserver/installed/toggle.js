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
function toggle() {
	app.dump("toggle");
	
	
	// like AJAX
	var req = new CoapRequest();
	
	// a timeout handler to abort before the 5sec interval
	req.timeout = 4900;
	req.ontimeout = function() {
			app.dump("Toggler timed out!");
			
			var request = new CoapRequest();
			request.open("PUT", "coap://econotag3.local/actuators/leds?color=g", true);
			request.onload = null;
			request.send("mode=on");
		};
	
	// actual toggling
	req.open("POST", "coap://sky1.local/actuators/toggle", false); // synchronous
	req.send();
	
	app.dump("done");
}

app.setInterval(toggle, 5000);

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
var uri = 'coap://localhost/'; // set via POST
var method = 'GET'; // set via POST

app.dump('Set RTT URI via POST');

function pollNode() {
	var client = new CoapRequest();
	client.timeout = 60000;
	
	var min = client.timeout*1000000;
	var max = 0;
	var total = 0;
	
	var sent = 0;
	
	for (var i=0; i<1000; ++i) {
		client.open(method, uri, false);
		var t0 = app.getNanoTime();
		client.send('');
		var dt = (app.getNanoTime()-t0)/1000000;
		app.dump('time='+dt+'ms');
		++sent;
		total += dt;
		
		if (dt < min) min = dt;
		if (dt > max) max = dt;
	}
	
	app.dump('');
	app.dump('RTT statistics for '+uri+':');
	app.dump('    Packets: Sent = '+sent+', Received = '+sent+', Lost = 0 (0% loss),');
	app.dump('Approximate round trip times in milli-seconds:');
	app.dump('    Minimum = '+min+'ms, Maximum = '+max+'ms, Average = '+(total/sent)+'ms');
	
	return 'RTT\t'+min+'\t'+max+'\t'+(total/sent);
}

app.root.onget = function(request) {
	request.accept();
	
	dump('RTT waiting...');
	
	app.sleep(1000);

	request.respond(69, pollNode());
}

app.root.onpost = function(request) {
	var value = request.requestText.split(' ');
	if (value.length != 2 && value.length != 1) {
		request.respond(128);
		return
	}
	uri = value[value.length-1];
	if (value.length == 2) {
		method = value[0];
	}
	app.dump('RTT URI: ' + uri);
	request.respond(68);
}

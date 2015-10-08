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
 * This app counts the POST Request it gets. On a PUT Request the counter is
 * resetted to 0. This app is to test whether handleGET/POST/PUT/DELETE are
 * overridable and what happens if they aren't.
 */

var count = 0;

app.root.onget = function(request) {
	request.respond(2.05, "counter: "+count);
};

app.root.ondelete = function(request) {
	count = 0;
	app.dump("reset counter to 0");
	request.respond(2.02, "");
};

app.root.onpost = function(request) {
	count = count + 1;
	request.respond(2.04, "counter: "+count);
};


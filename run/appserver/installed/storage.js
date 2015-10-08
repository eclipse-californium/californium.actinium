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
 * Storage is a test app for the AppServer.
 * Send a POST request with a name as payload to create a new subresource with the specified name.
 * Send a PUT request with a text as payload to store the specified text inside the resource.
 * Send a GET request to retrieve the text from the resource.
 * Same semantics applys to all created subresources.
 */

var subress = new Array();
var paths = new Array();

var content = ""; //java.lang.reflect.Array.newInstance(java.lang.Character.TYPE, 0);

app.root.onpost = function(request) {
	var name = request.requestText;
	
	if (contains(subress,name)) {
		var path = findPathForName(subress, paths, name);
		request.setLocationPath(path);
		request.respond(4.00, "Storage "+name+" is already created at "+path);
	} else {
		var storage = new Storage(name);
		app.root.add(storage.res);
		var path = storage.res.getURI();
		subress[subress.length] = name;
		paths[paths.length] = path;
		
		request.setLocationPath(path);
		request.respond(2.01, "Storage "+name+" created at location "+path);
	}
}

app.root.onput = function(request) {
	content = request.requestText;
	app.root.changed();
	request.respond(2.04);
}

app.root.onget = function(request) {
	request.respond(2.05, content);
}

app.root.ondelete = function(request) {
	request.respond(4.03, "Storage root cannot be deleted");
}

function Storage(name) {
	this.res = new JavaScriptResource(name);
	this.subress = new Array();
	this.paths = new Array();
	this.content = "";
	var mythis = this;
	
	this.res.onget = function(request) {
		request.respond(2.05, mythis.content);
	}
	
	this.res.onpost = function(request) {
		var name = request.requestText;

		if (contains(mythis.subress, name)) {
			request.setLocationPath(findPathForName(mythis.subress, mythis.paths, name));
			request.respond(4.00, "Storage "+name+" is already created");
		} else {
			var storage = new Storage(name);
			mythis.res.add(storage.res);
			var path = storage.res.getURI();
			mythis.subress[mythis.subress.length] = name;
			mythis.paths[mythis.paths.length] = path;

			request.setLocationPath(path);
			request.respond(2.01, "Storage "+name+" created at location "+path);
		}
	}
	
	this.res.onput = function(request) {
		mythis.content = request.requestText;
		mythis.res.changed();
		request.respond(2.04);
	}
	
	this.res.ondelete = function(request) {
		mythis.res.delete();
		request.respond("Deleted");
	}
}

function contains(array, name) {
	for (var i=0;i<subress.length;i++) {
		if (name==array[i]) {
			return true;
		}
	}
	return false;
}

function findPathForName(ress, ps, n) {
	for (var i=0;i<ress.length;i++) {
		if (ress[i] == n) {
			return ps[i];
		}
	}
	return null;
}

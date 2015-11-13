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
 * This resource provides the date and time from differenz timezones E.g. there
 * is a resource for the Central European Time. It responds to a GET Request
 * with the date and time of the timezone in the format DD.MM.YYYY - hh.mm.ss.
 * You can also send a GET request to a timezone resource's dateResource and
 * receive only the date or to a resource's timeResource and receive only the
 * time. You also can change the date or time of dateResource and timeResource
 * respectively by sending a PUT request, containing the desired new date or
 * time as payload in the same format as the resource responds to a GET request.
 */

function MyDate(ts) {
	this.date = new Date(ts);
	this.timestamp = System.currentTimeMillis();
	this.id = "dateResource";
	this.getId = function() {
		return this.id;
	};
	this.getDate = function() {
		var ts = app.getTime();
		this.date.setTime(this.date.getTime() + (ts-this.timestamp));
		this.timestamp = ts;
		
		var df = new SimpleDateFormat("dd.MM.yyyy");
		var formattedDate = df.format(this.date);
		return formattedDate;
	};
	this.setDate = function(str_date) {
		var formatter = new SimpleDateFormat("dd.MM.yyyy");
		this.date = formatter.parse(str_date);
		this.timestamp = System.currentTimeMillis();
	};
	this.getDescription = function() {
		return "I return the current date";
	};
	
	this.resDate = new JavaScriptResource(this.getId());
	this.resDate.setTitle(this.getDescription());

	var mythis = this;
	this.resDate.onget = function(request) {
		request.respond(2.05,mythis.getDate());
	};
	
	this.resDate.onput = function(request) {
		try {
			var str_date = request.requestText;
			mythis.setDate(str_date);
			request.respond(2.05, mythis.getDate());

		} catch (e if e.javaException instanceof ParseException) {				
			request.respond(4.00, mythis.getDate());
		}
	};
}

function MyTime(ts) {
	
	this.time = new Date(ts);
	this.timestamp = app.getTime();
	this.id = "timeResource";
	this.getId = function() {
		return this.id;
	};
	this.getTime = function() {
		var ts = app.getTime();
		this.time.setTime(this.time.getTime() + (ts-this.timestamp));
		this.timestamp = ts;
		
		var df = new SimpleDateFormat("kk:mm:ss");
		var formattedDate = df.format(this.time);
		return formattedDate;
	};
	this.setTime = function(str_time) {
		var formatter = new SimpleDateFormat("kk:mm:ss");
		this.time = formatter.parse(str_time);
		this.timestamp = app.getTime();
	};
	this.getDescription = function() {
		return "I return the current time";
	};
	
	this.resTime = new JavaScriptResource(this.getId());
	this.resTime.setTitle(this.getDescription());
	this.resTime.setObservable(true);
	
	var mythis = this;
	
	app.setInterval(function() { mythis.resTime.changed(); }, 5000);
	
	this.resTime.onget = function(request) {
			request.respond(2.05,mythis.getTime());
	};
	
	this.resTime.onput = function(request) {
		try {
			var str_time = request.requestText;
			mythis.setTime(str_time);
			request.respond(2.05,mythis.getTime());

		} catch (e if e.javaException instanceof ParseException) {
			request.respond(4.00, mythis.getTime());
		}
	};
}

function DateTime(ts,id) {
	this.date = new MyDate(ts);
	this.time = new MyTime(ts);
	
	this.resource = new JavaScriptResource(id.replace(/ /g,'_'));
	
	var mythis = this;
	this.resource.onget = function(request) {
		request.respond(2.05, mythis.date.getDate()+" - "+mythis.time.getTime());
	};
	
	this.resource.add(this.date.resDate);
	this.resource.add(this.time.resTime);
}

DateTime.prototype.getDescription = function() {
	return "I return the date and time";
}

var hour = 60*60*1000;
var utc = app.getTime() - hour;

var datetimePST = new DateTime(utc + 9*hour,"Korea Standard Time");
var datetimeCET = new DateTime(utc + 1*hour,"Central European Time");
var datetimeUTC = new DateTime(utc,"Coordinated Universal Time");
var datetimeEST = new DateTime(utc - 5*hour,"Eastern Standard Time");
var datetimeKST = new DateTime(utc - 8*hour,"Pacific Standart Time");

app.root.add(datetimePST.resource);
app.root.add(datetimeCET.resource);
app.root.add(datetimeUTC.resource);
app.root.add(datetimeEST.resource);
app.root.add(datetimeKST.resource);
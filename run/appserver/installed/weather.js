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
function Property(resid, dflt) {
	var THIS = this;
	this.value = dflt;
	this.res = new JavaScriptResource(resid);
	this.res.setObservable(true);
	this.set = function(val) {
		THIS.value = val;
		THIS.res.changed();
	};
	this.res.onget = function(request) {
		request.respond(2.05, THIS.value);
	};
	this.res.onput = function(request) {
		THIS.set(request.requestText);
		request.respond(2.05, THIS.value);
	};
}

var HOST = "http://api.wunderground.com/api//"; // TODO add your API key between //
var FEATURE = "conditions";

var location = app.getProperty("weatherlocation", "China/Shanghai");
var propLocation = new Property("location", location);
var propTemperature = new Property("temperature", 0);
var propHumidity = new Property("humidity", 0);
var propWind = new Property("wind", 0);

app.root.add(propLocation.res);
app.root.add(propTemperature.res);
app.root.add(propHumidity.res);
app.root.add(propWind.res);

var rootText = propLocation.value;

function getWeather() {
	var query = HOST+FEATURE+"/q/"+propLocation.value+".json";
	
	app.dump("Getting "+query);
	
	////var xhr = new XMLHttpRequest();
	////xhr.open("GET", query, false);
	////xhr.send();
	//
	//var response = JSON.parse(xhr.responseText);
	//
	//
	//propTemperature.set( response.current_observation.temp_c );
	//propHumidity.set( response.current_observation.relative_humidity );
	//propWind.set( response.current_observation.wind_kph );
	//
	//rootText = response.current_observation.display_location.city+": "+response.current_observation.temperature_string+", "+response.current_observation.relative_humidity+" humidity, Wind "+response.current_observation.wind_string;
	//app.dump("Got "+rootText);
}

app.root.onget = function(request) {
	request.respond(2.05, rootText);
};

getWeather();

// update weather info every 5 minutes
app.setInterval( getWeather, 5*60*1000);

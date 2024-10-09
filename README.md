# !!! DISCONTINUED !!!

This subproject is not longer maintained.

Actinium (Ac) App-server 2.0 for Californium
============================================

## Version 2.0 is now based on Nashorn and provides module loading mechanism

Our novel runtime container Actinium (Ac) exposes scripts, their configuration, and their lifecycle management through a RESTful programming interface using the Constrained Application Protocol (CoAP).
We endow the JavaScript language with an API for direct interaction with mote-class IoT devices, the CoapRequest object, and means to export script data as Web resources.
With Actinium, applications can be created by simply mashing up resources provided by CoAP servers on devices, other scripts, and classic Web services.

Maven
-----

Actinium can only be built using JDK 1.8 because of its dependencies to some JDK internal classes
(you are more than welcome to help us getting rid of them).
Use `mvn clean install` in the root directory to build the Actinium (Ac) server.
A standalone JAR will be created in the ./run/ directory.

The dependencies are available from the default Maven repositories.

Run with `java -jar actinium-*.jar`.

If newer JDKs are used to run maven, `-DuseToolchain` can be used to switch back to the required JDK 1.8

`mvn clean install -DuseToolchain`

Eclipse
-------

The project also includes the project files for Eclipse. Make sure to have the
following before importing the Californium (Cf) projects:

* [Eclipse EGit](http://www.eclipse.org/egit/)
* [m2e - Maven Integration for Eclipse](http://www.eclipse.org/m2e/)
* UTF-8 workspace text file encoding (Preferences &raquo; General &raquo; Workspace)

Then choose *[Import... &raquo; Git &raquo; Projects from Git &raquo; Local]*
to import `californium.actinium` into Eclipse.

Run `org.eclipse.californium.actinium.AcServer` as Java application.

Usage
-----

### Prerequisites

Make sure the `./appserver/` directory with `config.cfg` exists when you run
Actinium (Ac), which can be found in the `./run/` directory (must be copied
when running from Eclipse).
`./appserver/installed/` contains several example scripts.
`./appserver/apps/` contains the configurations of app instances.
Some of the start automatically depending on the `start_on_startup` flag.

### Installing a New App

Send a POST with JavaScript code to `install?[appname]` where [appname] is
used as (file)name for the app code. 

Example:

		Request:  POST coap://localhost:5683/install?helloWorld
		Payload:  app.root.onget = function(request) {
	                  request.respond(2.05, "Hello World");
		          }
		Response: Application appname successfully installed to /install/helloWorld

The example app ist stored in `./appserver/installed/` as `helloWorld.js`.
The server now has a new resource /install/helloWorld.

### Starting a New App Instance

Send a POST with "name=[instance name]" to /install/helloWorld where
[instance name] is used as name of the instance as well as filename for the
configuration. You can instantiate an installed app several times by choosing
different instance names.

Example:

		Request:  POST coap://localhost:5683/install/helloWorld
		Payload:  name=hello-1
		Response: Application hello-1 successfully installed to /apps/running/hello-1

The example instance is stored in `./appserver/apps/` as `config_hello-1.cfg`.
The server now has two new resources, /apps/appconfigs/hello-1 to change the
configuration of the instance and /apps/running/hello-1 to interact with the
running app.

### Communicating With a Running App

Send GET, POST, PUT, or DELETE to a resource under /apps/running/ depending on
what functionality was implemented in the JavaScript code.

### Configuring an App Instance

Some of the instance properties can be changed during runtime, some are fixed
(e.g., the name). The changes are committed to the config resource of the app
under /apps/appconfigs/. For this, use a POST with `key = value` pairs as
payload to update existing keys. Use PUT to clear all existing keys and start
fresh with the keys in the PUT payload.

There are also three special commands to control the running instance via POST:
`start`, `restart`, and `stop`. 

Example:

		a) Request:  GET coap://localhost:5683/apps/appconfigs/hello-1
		   Response: All app properties
		   
		b) Request:  POST coap://localhost:5683/apps/appconfigs/hello-1
		   Payload:  enable_request_delivery = false
		   Response: successfully changed keys: [enable_request_delivery]
		   [App hello-1 cannot receive any more request]
		   
		c) Request:  POST coap://localhost:5683/apps/appconfigs/hello-1
		   Payload:  restart
		   Response: put running = restart
		   [App hello-1 is restarting]

### Programming Your Own Apps

To respond to CoAP requests, simply set the following onX functions on a
`JavaScriptResource`. They have the request as argument, which can be responded
through its `respond(code, payload)` method.
`app.root` is the root resource of your app under /apps/running/<instance name>.

Example:

		app.root.onget = fnc;
		app.root.onpost = fnc;
		app.root.onput = fnc;
		app.root.ondelete = fnc;
		
		fnc = function(request) { 
			request.respond(2.05, "Hello World"); 
		}

Sub-resources can be added to the root resource as shown below:

		res = new JavaScriptResource('mySubResource');
		app.root.add(res);

You can dump information to the console through the app object:

		app.dump("Hello World");
		app.error("Houston, we have a problem");

### AcShell

The `AcShell` can be used to run a single app from the command line.
Start with:

	Windows:
		java -cp target/actinium-*.jar org.eclipse.californium.actinium.AcShell file.js
	
	Linux:
		java -cp target/actinium-*.jar org.eclipse.californium.actinium.AcShell file.js

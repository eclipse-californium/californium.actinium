package org.eclipse.californium.actinium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.californium.actinium.cfg.Config;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;

public class HelloWorldTest extends BaseServerTest {


	@Test
	public void testHelloWorldApp() throws Exception {
		testInstallHelloWorld();
		createHello1Instance();
		testCheckIfHello1InstanceExists();
		testCheckHello1Instance();
		Thread.sleep(2000);
		testCheckIfHello1IsRunning();
		testHello1Instance();
	}

	private void testCheckIfHello1InstanceExists() throws InterruptedException {
		Request getapps2 = Request.newGet();
		getapps2.setURI(baseURL+"apps/instances");
		getapps2.send();
		Response runningApps = getapps2.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.CONTENT, runningApps.getCode());
		String content = runningApps.getPayloadString();
		assertTrue("Response contains \"hello-1\"", content.contains("hello-1"));
	}

	private void testCheckHello1Instance() throws InterruptedException {
		Request getapps2 = Request.newGet();
		getapps2.setURI(baseURL+"apps/instances/hello-1");
		getapps2.send();
		Response runningApps = getapps2.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.CONTENT, runningApps.getCode());
		String content = runningApps.getPayloadString();
		assertTrue("Response contains \"name: hello-1\"", content.contains("name: hello-1"));
		assertTrue("Response contains \"app: helloWorld\"", content.contains("app: helloWorld"));
	}

	private void testCheckIfHello1IsRunning() throws InterruptedException {
		Request getapps2 = Request.newGet();
		getapps2.setURI(baseURL+"apps/running");
		getapps2.send();
		Response runningApps = getapps2.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.CONTENT, runningApps.getCode());
		assertEquals("hello-1\n", runningApps.getPayloadString());
	}

	@Test
	public void testServerRestart() throws Exception {
		testInstallHelloWorld();
		createHello1Instance();
		Thread.sleep(2000);
		testHello1Instance();
		stopServer();
		startServer();
		Thread.sleep(2000);
		testHello1Instance();
	}

	private void testHello1Instance() throws InterruptedException {
		testHello1Instance("Hello World");
	}

	private void testHello1Instance(String expectedPayload) throws InterruptedException {
		Request getapps2 = Request.newGet();
		getapps2.setURI(baseURL+"apps/running/hello-1");
		getapps2.send();
		Response responseApps2 = getapps2.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.CONTENT, responseApps2.getCode());
		assertEquals(expectedPayload, responseApps2.getPayloadString());
	}

	@Test
	public void testUpdateApp() throws Exception {
		testHelloWorldApp();
		String scriptName = "helloWorld";
		String script = "app.root.onget = function(request) {\n"+
				"                  request.respond(2.05, \"Hello World2\");\n"+
				"              }";
		Request updateApp = Request.newPut();
		updateApp.setURI(baseURL+"install/"+scriptName);
		updateApp.setPayload(script);
		updateApp.send();
		assertEquals(CoAP.ResponseCode.CHANGED, updateApp.waitForResponse(100).getCode());

		Thread.sleep(2000);

		testHello1Instance("Hello World2");
	}

	@Test
	public void testDeleteApp() throws Exception {
		testHelloWorldApp();

		Request newapp = Request.newDelete();
		newapp.setURI(baseURL+"install/helloWorld");
		newapp.send();
		Response response = newapp.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.DELETED, response.getCode());

		Thread.sleep(2000);

		Request getapps2 = Request.newGet();
		getapps2.setURI(baseURL+"apps/running/hello-1");
		getapps2.send();
		Response responseApps2 = getapps2.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.NOT_FOUND, responseApps2.getCode());
	}

	@Test
	public void testInstallHelloWorld() throws InterruptedException {
		String scriptName = "helloWorld";
		String script = "app.root.onget = function(request) {\n"+
				"                  request.respond(2.05, \"Hello World\");\n"+
				"              }";
		installScript(scriptName, script);

		Request getapps = Request.newGet();
		getapps.setURI(baseURL+"install/");
		getapps.send();
		Response responseApps = getapps.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.CONTENT, responseApps.getCode());
		assertEquals("helloWorld\n", responseApps.getPayloadString());
	}

	@Test
	public void testInstallHelloWorldTwice() throws InterruptedException {
		testInstallHelloWorld();
		String scriptName = "helloWorld";
		String script = "app.root.onget = function(request) {\n"+
				"                  request.respond(2.05, \"Hello World\");\n"+
				"              }";
		Request newapp = Request.newPost();
		newapp.setURI(baseURL+"install?"+scriptName);
		newapp.setPayload(script);
		newapp.send();
		Response response = newapp.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.BAD_REQUEST, response.getCode());
		assertEquals("The given name helloWorld is already in use. Choose another name or update the current app with a PUT request", response.getPayloadString());

	}

	@Test
	public void testTwoInstancesWithTheSameName() throws Exception {
		testInstallHelloWorld();
		createHello1Instance();

		Request installApp2 = Request.newPost();
		installApp2.setURI(baseURL+"install/helloWorld");
		installApp2.setPayload("name=hello-1");
		installApp2.send();
		Response responseInstallApp2 = installApp2.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.BAD_REQUEST, responseInstallApp2.getCode());
		assertEquals("The name hello-1 is already in use for an app. Please specify a new name", responseInstallApp2.getPayloadString());

	}

	private void createHello1Instance() throws InterruptedException {
		Request installApp = Request.newPost();
		installApp.setURI(baseURL+"install/helloWorld");
		installApp.setPayload("name=hello-1");
		installApp.send();
		Response responseInstallApp = installApp.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.CREATED, responseInstallApp.getCode());
		assertEquals("Application helloWorld successfully installed to /apps/running/hello-1", responseInstallApp.getPayloadString());
	}

}

package org.eclipse.californium.actinium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.junit.Test;

public class HelloWorldTest extends BaseServerTest {


	@Test
	public void testHelloWorldApp() throws Exception {
		testInstallHelloWorld();
		createInstance("helloWorld", "hello-1");
		testCheckIfInstanceExists("hello-1");
		testCheckInstance("helloWorld", "hello-1");
		Thread.sleep(2000);
		testCheckIfInstanceIsRunning("hello-1");
		testHello1Instance("Hello World");
	}

	@Test
	public void testServerRestart() throws Exception {
		testInstallHelloWorld();
		createInstance("helloWorld", "hello-1");
		Thread.sleep(2000);
		testHello1Instance("Hello World");
		stopServer();
		startServer();
		Thread.sleep(2000);
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
		createInstance("helloWorld", "hello-1");

		Request installApp2 = Request.newPost();
		installApp2.setURI(baseURL+"install/helloWorld");
		installApp2.setPayload("name=hello-1");
		installApp2.send();
		Response responseInstallApp2 = installApp2.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.BAD_REQUEST, responseInstallApp2.getCode());
		assertEquals("The name hello-1 is already in use for an app. Please specify a new name", responseInstallApp2.getPayloadString());

	}

}

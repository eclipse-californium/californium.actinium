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
		testInstallHelloWorld("helloWorld");
		createInstance("helloWorld", "hello-1");
		testCheckIfInstanceExists("hello-1");
		testCheckInstance("helloWorld", "hello-1");
		Thread.sleep(2000);
		testCheckIfInstanceIsRunning("hello-1");
		testGET("apps/running/hello-1", "Hello World");
	}

	@Test
	public void testServerRestart() throws Exception {
		testInstallHelloWorld("helloWorld");
		createInstance("helloWorld", "hello-1");
		Thread.sleep(2000);
		testGET("apps/running/hello-1", "Hello World");
		stopServer();
		startServer();
		Thread.sleep(2000);
		testGET("apps/running/hello-1", "Hello World");
	}

	@Test
	public void testUpdateApp() throws Exception {
		testHelloWorldApp();
		String scriptName = "helloWorld";
		String script = "app.root.onget = function(request) {\n"+
				"                  request.respond(ResponseCode.CONTENT, \"Hello World2\");\n"+
				"              }";
		Request updateApp = Request.newPut();
		updateApp.setURI(baseURL+"install/"+scriptName);
		updateApp.setPayload(script);
		updateApp.send();
		assertEquals(CoAP.ResponseCode.CHANGED, updateApp.waitForResponse(TIMEOUT).getCode());

		Thread.sleep(2000);

		testGET("apps/running/hello-1", "Hello World2");
	}

	@Test
	public void testDeleteApp() throws Exception {
		testHelloWorldApp();

		Request request = Request.newDelete();
		request.setURI(baseURL+"install/helloWorld");
		request.send();
		Response response = request.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.DELETED, response.getCode());

		Thread.sleep(2000);

		request = Request.newGet();
		request.setURI(baseURL+"apps/running/hello-1");
		request.send();
		response = request.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.NOT_FOUND, response.getCode());


		request = Request.newGet();
		request.setURI(baseURL+"apps/running/hello-1");
		request.send();
		response = request.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.NOT_FOUND, response.getCode());

		testCheckIfInstanceDoesNotExist("hello-1");
	}


	@Test
	public void testDeleteAppInstance() throws Exception {
		testHelloWorldApp();

		Request request = Request.newDelete();
		request.setURI(baseURL+"apps/instances/hello-1");
		request.send();
		Response response = request.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.DELETED, response.getCode());

		Thread.sleep(2000);

		request = Request.newGet();
		request.setURI(baseURL+"apps/running/hello-1");
		request.send();
		response = request.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.NOT_FOUND, response.getCode());


		request = Request.newGet();
		request.setURI(baseURL+"apps/running/hello-1");
		request.send();
		response = request.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.NOT_FOUND, response.getCode());

		testCheckIfInstanceDoesNotExist("hello-1");
	}

	@Test
	public void testInstallHelloWorldTwice() throws InterruptedException {
		testInstallHelloWorld("helloWorld");
		String scriptName = "helloWorld";
		String script = "app.root.onget = function(request) {\n"+
				"                  request.respond(ResponseCode.CONTENT, \"Hello World\");\n"+
				"              }";
		Request newapp = Request.newPost();
		newapp.setURI(baseURL+"install?"+scriptName);
		newapp.setPayload(script);
		newapp.send();
		Response response = newapp.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.BAD_REQUEST, response.getCode());
		assertEquals("The given app name helloWorld is already in use. Choose another name or update the current app with a PUT request", response.getPayloadString());

	}

	@Test
	public void testTwoInstancesWithTheSameName() throws Exception {
		testInstallHelloWorld("helloWorld");
		createInstance("helloWorld", "hello-1");

		Request installApp2 = Request.newPost();
		installApp2.setURI(baseURL+"install/helloWorld");
		installApp2.setPayload("name=hello-1");
		installApp2.send();
		Response responseInstallApp2 = installApp2.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.BAD_REQUEST, responseInstallApp2.getCode());
		assertEquals("The name hello-1 is already in use for an app. Please specify a new name", responseInstallApp2.getPayloadString());

	}

}

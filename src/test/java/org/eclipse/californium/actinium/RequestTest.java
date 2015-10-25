package org.eclipse.californium.actinium;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;

public class RequestTest extends BaseServerTest {

	@Test
	public void testInstallHelloWorld() throws InterruptedException, FileNotFoundException {
		//Install Request
		installScript("requestTest", new File("run/appserver/installed/request_test.js"));
		createInstance("requestTest", "requ");
		testCheckIfInstanceExists("requ");
		testCheckInstance("requestTest", "requ");
		//Install PostCounter
		installScript("postcounter", new File("run/appserver/installed/postcounter.js"));
		createInstance("postcounter", "counter");
		Thread.sleep(3000);
		testCheckIfInstanceIsRunning("requ");
		testCheckIfInstanceIsRunning("counter");
		Request configureRTT = Request.newPost();
		configureRTT.setURI(baseURL + "apps/running/requ");
		configureRTT.setPayload("POST coap://" + baseURL + "apps/running/counter");
		configureRTT.send();

		Thread.sleep(1000);
		assertEquals(CoAP.ResponseCode.CHANGED, configureRTT.waitForResponse(TIMEOUT).getCode());
		Request runRTT = Request.newGet();
		runRTT.setURI(baseURL + "apps/running/requ");
		runRTT.send();
		Response result = runRTT.waitForResponse(TIMEOUT*10000);
		assertEquals(CoAP.ResponseCode.CONTENT, result.getCode());
		assertEquals("OK", result.getPayloadString());
		Request checkCounter = Request.newGet();
		checkCounter.setURI(baseURL+"apps/running/counter");
		checkCounter.send();
		Response counterResult = checkCounter.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.CONTENT, counterResult.getCode());
		String counterString = counterResult.getPayloadString();
		assertEquals("counter: 1", counterString);

	}

	@Test
	public void testTimeout() throws InterruptedException, FileNotFoundException {
		//Install Request
		installScript("requestTest", new File("run/appserver/installed/request_test.js"));
		createInstance("requestTest", "requ");
		testCheckIfInstanceExists("requ");
		testCheckInstance("requestTest", "requ");
		Thread.sleep(3000);
		testCheckIfInstanceIsRunning("requ");
		Request configureRTT = Request.newPost();
		configureRTT.setURI(baseURL + "apps/running/requ");
		configureRTT.setPayload("POST coap://localhost:2222");
		configureRTT.send();

		Thread.sleep(3000);
		assertEquals(CoAP.ResponseCode.CHANGED, configureRTT.waitForResponse(TIMEOUT).getCode());
		Request runRTT = Request.newGet();
		runRTT.setURI(baseURL + "apps/running/requ");
		runRTT.send();
		Response result = runRTT.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.CONTENT, result.getCode());
		assertEquals("TIMEOUT", result.getPayloadString());

	}


}

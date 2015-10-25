package org.eclipse.californium.actinium;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class RequestTest extends BaseServerTest {

	private final boolean async;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{true},{false}
		});
	}


	public RequestTest(Boolean async) {
		this.async = async;
	}

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
		configureRTT.setPayload("POST coap://" + baseURL + "apps/running/counter " + async);
		configureRTT.send();
		if(async) {
			Thread.sleep(1000);
		}
		assertEquals(CoAP.ResponseCode.CHANGED, configureRTT.waitForResponse(TIMEOUT * 30).getCode());
		Request runRTT = Request.newGet();
		runRTT.setURI(baseURL + "apps/running/requ");
		runRTT.send();
		Response result = runRTT.waitForResponse(TIMEOUT * 10000);
		assertEquals(CoAP.ResponseCode.CONTENT, result.getCode());
		assertEquals("OK", result.getPayloadString());
		Request checkCounter = Request.newGet();
		checkCounter.setURI(baseURL + "apps/running/counter");
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
		configureRTT.setPayload("POST coap://localhost:2222 " + async);
		configureRTT.send();
		if(async) {
			Thread.sleep(3000);
		}
		assertEquals(CoAP.ResponseCode.CHANGED, configureRTT.waitForResponse(TIMEOUT*30).getCode());
		Request runRTT = Request.newGet();
		runRTT.setURI(baseURL + "apps/running/requ");
		runRTT.send();
		Response result = runRTT.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.CONTENT, result.getCode());
		assertEquals("TIMEOUT", result.getPayloadString());

	}


	@Test
	public void testAbort() throws InterruptedException, FileNotFoundException {
		if(!async)
			return;
		//Install Request
		installScript("requestTest", new File("run/appserver/installed/request_test.js"));
		createInstance("requestTest", "requ");
		testCheckIfInstanceExists("requ");
		testCheckInstance("requestTest", "requ");
		Thread.sleep(3000);
		testCheckIfInstanceIsRunning("requ");
		Request configureRTT = Request.newPost();
		configureRTT.setURI(baseURL + "apps/running/requ");
		configureRTT.setPayload("POST coap://localhost:2222 true true");
		configureRTT.send();
		if(async) {
			Thread.sleep(3000);
		}
		assertEquals(CoAP.ResponseCode.CHANGED, configureRTT.waitForResponse(TIMEOUT*30).getCode());
		Request runRTT = Request.newGet();
		runRTT.setURI(baseURL + "apps/running/requ");
		runRTT.send();
		Response result = runRTT.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.CONTENT, result.getCode());
		assertEquals("0", result.getPayloadString());

	}


}

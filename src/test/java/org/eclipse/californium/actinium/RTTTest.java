package org.eclipse.californium.actinium;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RTTTest extends BaseServerTest {

	@Test
	public void testRTT() throws InterruptedException, FileNotFoundException {
		//Install RTT
		installScript("rttTest", new File("run/appserver/installed/rtt.js"));
		createInstance("rttTest", "rtt-1");
		testCheckIfInstanceExists("rtt-1");
		testCheckInstance("rttTest", "rtt-1");
		//Install PostCounter
		installScript("postcounter", new File("run/appserver/installed/postcounter.js"));
		createInstance("postcounter", "counter");
		Thread.sleep(3000);
		testCheckIfInstanceIsRunning("rtt-1");
		testCheckIfInstanceIsRunning("counter");
		Request configureRTT = Request.newPost();
		configureRTT.setURI(baseURL+"apps/running/rtt-1");
		configureRTT.setPayload("POST coap://"+baseURL+"apps/running/counter");
		configureRTT.send();

		assertEquals(CoAP.ResponseCode.CHANGED, configureRTT.waitForResponse(TIMEOUT).getCode());
		Request runRTT = Request.newGet();
		runRTT.setURI(baseURL+"apps/running/rtt-1");
		runRTT.send();
		Response result = runRTT.waitForResponse(TIMEOUT*10000);
		assertEquals(CoAP.ResponseCode.CONTENT, result.getCode());
		Request checkCounter = Request.newGet();
		checkCounter.setURI(baseURL+"apps/running/counter");
		checkCounter.send();
		Response counterResult = checkCounter.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.CONTENT, counterResult.getCode());
		String counterString = counterResult.getPayloadString();
		assertEquals("counter: 1000", counterString);

	}


}

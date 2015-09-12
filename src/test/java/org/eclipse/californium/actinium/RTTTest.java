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
	public void testInstallHelloWorld() throws InterruptedException, FileNotFoundException {
		String scriptName = "rttTest";
		installScript(scriptName, new File("run/appserver/installed/rtt.js"));
		createInstance(scriptName, "rtt-1");
		testCheckIfInstanceExists("rtt-1");
		testCheckInstance("rttTest", "rtt-1");
		Thread.sleep(2000);
		testCheckIfInstanceIsRunning("rtt-1");
	}


}

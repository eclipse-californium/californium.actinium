package org.eclipse.californium.actinium;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;

public class ModuleTest extends BaseServerTest {

	@Test
	public void testModule() throws InterruptedException, FileNotFoundException {
		installLibrary("test_lib", new File(BaseServerTest.TEST_APP_FOLDER + "/src/test_lib.js"));
		installScript("test_module", new File(BaseServerTest.TEST_APP_FOLDER + "/src/test_module.js"));
		createInstance("test_module", "test_module");
		testCheckIfInstanceExists("test_module");
		testCheckInstance("test_module", "test_module");
		Thread.sleep(3000);
		testCheckIfInstanceIsRunning("test_module");
		Request checkCounter = Request.newGet();
		checkCounter.setURI(baseURL + "apps/running/test_module");
		checkCounter.send();
		Response counterResult = checkCounter.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.CONTENT, counterResult.getCode());
		String counterString = counterResult.getPayloadString();
		assertEquals("test_lib", counterString);
	}
}

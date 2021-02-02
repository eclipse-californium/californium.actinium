/*******************************************************************************
 * Copyright (c) 2014 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Yassin N. Hassan - initial implementation
 *    Matthias Kovatsch - creator and main architect
 ******************************************************************************/
package org.eclipse.californium.actinium;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.elements.rule.ThreadsRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;

public class ModuleTest extends BaseServerTest {
	@ClassRule
	public static ThreadsRule cleanup = new ThreadsRule(THREADS_RULE_FILTER);

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

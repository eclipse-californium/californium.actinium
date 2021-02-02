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
 ******************************************************************************/
package org.eclipse.californium.actinium;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.elements.rule.ThreadsRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ExtendTest extends BaseServerTest {
	@ClassRule
	public static ThreadsRule cleanup = new ThreadsRule(THREADS_RULE_FILTER);


    private final String script;

    public ExtendTest(String script) {
        this.script = script;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"test_extend"},
                {"test_extend2"},
                {"test_extend3"},
                {"test_multi_extend"},
                {"test_multi_extend2"},
                {"test_multi_extend3"}
        });
    }

    @Test
    public void testExtend() throws InterruptedException, FileNotFoundException {
        installScript(script, new File("run/appserver/installed/" + script + ".js"));
        createInstance(script, script);
        testCheckIfInstanceExists(script);
        testCheckInstance(script, script);
        Thread.sleep(3000);
        testCheckIfInstanceIsRunning(script);
        Request checkCounter = Request.newGet();
        checkCounter.setURI(baseURL + "apps/running/" + script);
        checkCounter.send();
        Response counterResult = checkCounter.waitForResponse(TIMEOUT*10);
        assertEquals(CoAP.ResponseCode.CONTENT, counterResult.getCode());
        String counterString = counterResult.getPayloadString();
        assertEquals("OK", counterString);

    }


}
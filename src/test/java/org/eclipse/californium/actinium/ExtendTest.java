package org.eclipse.californium.actinium;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;

public class ExtendTest extends BaseServerTest {

    @Test
    public void testExtend() throws InterruptedException, FileNotFoundException {
        installScript("test_extend", new File("run/appserver/installed/test_extend.js"));
        createInstance("test_extend", "test_extend");
        testCheckIfInstanceExists("test_extend");
        testCheckInstance("test_extend", "test_extend");
        Thread.sleep(3000);
        testCheckIfInstanceIsRunning("test_extend");
        Request checkCounter = Request.newGet();
        checkCounter.setURI(baseURL+"apps/running/test_extend");
        checkCounter.send();
        Response counterResult = checkCounter.waitForResponse(TIMEOUT);
        assertEquals(CoAP.ResponseCode.CONTENT, counterResult.getCode());
        String counterString = counterResult.getPayloadString();
        assertEquals("OK", counterString);

    }


}
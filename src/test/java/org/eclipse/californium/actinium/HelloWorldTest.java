package org.eclipse.californium.actinium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.californium.actinium.cfg.Config;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class HelloWorldTest {


	private AcServer server;
	private int serverPort;
	private String baseURL;
	private File appFolder;
	
	@Before
	public void setUp() throws Exception {
		appFolder = new File("appserver_tmp");
		delete(appFolder);
		appFolder.mkdir();
		appFolder.deleteOnExit();
		new File("appserver_tmp/installed").mkdir();
		new File("appserver_tmp/apps").mkdir();
		PrintWriter writer = new PrintWriter("appserver_tmp/config.cfg", "UTF-8");
		writer.println("#Tue Sep 30 14:47:47 CEST 2014\n"+
				"apps_resource_id=apps\n"+
				"app_config_prefix=config_\n"+
				"app_config_path=appserver_tmp/apps/\n"+
				"javascript_suffix=.js\n"+
				"app_path=appserver_tmp/installed/\n"+
				"app_config_suffix=.cfg\n"+
				"install_resource_id=install\n"+
				"config_resource_id=config\n"+
				"app_config_resources=instances\n"+
				"running_resource_id=running\n"+
				"stats_resource_id=stats\n"+
				"start_on_install=true");
		writer.close();
		Config config = new Config("appserver_tmp/config.cfg");
		server = new AcServer(config);
		server.start();
		serverPort = server.getEndpoints().get(0).getAddress().getPort();
		baseURL = "localhost:"+serverPort+"/";

	}

	@After
	public void shutDownEndpoint() throws IOException {
		server.stop();
		delete(appFolder);
	}

	@Test
	public void testHelloWorldApp() throws Exception {
		Request newapp = Request.newPost();
		newapp.setURI(baseURL+"install?helloWorld");
		newapp.setPayload("app.root.onget = function(request) {\n"+
				"                  request.respond(2.05, \"Hello World\");\n"+
				"              }");
		newapp.send();
		Response response = newapp.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.CREATED, response.getCode());
		assertEquals("Application helloWorld successfully installed to /install/helloWorld", response.getPayloadString());

		Request getapps = Request.newGet();
		getapps.setURI(baseURL+"install/");
		getapps.send();
		Response responseApps = getapps.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.CONTENT, responseApps.getCode());
		assertEquals("helloWorld\n", responseApps.getPayloadString());



		Request installApp = Request.newPost();
		installApp.setURI(baseURL+"install/helloWorld");
		installApp.setPayload("name=hello-1");
		installApp.send();
		Response responseInstallApp = installApp.waitForResponse();
		assertEquals(CoAP.ResponseCode.CREATED, responseInstallApp.getCode());
		assertEquals("Application helloWorld successfully installed to /apps/running/hello-1", responseInstallApp.getPayloadString());
		Thread.sleep(2000);

		Request getapps2 = Request.newGet();
		getapps2.setURI(baseURL+"apps/running/hello-1");
		getapps2.send();
		Response responseApps2 = getapps2.waitForResponse();
		assertEquals(CoAP.ResponseCode.CONTENT, responseApps2.getCode());
		assertEquals("Hello World", responseApps2.getPayloadString());

	}

	void delete(File f) throws IOException {
		if (!f.exists())
			return;
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
		if (!f.delete())
			throw new FileNotFoundException("Failed to delete file: "+f);
	}
}

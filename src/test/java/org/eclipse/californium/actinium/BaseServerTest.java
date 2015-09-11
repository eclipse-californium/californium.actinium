package org.eclipse.californium.actinium;

import org.eclipse.californium.actinium.cfg.Config;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;


public class BaseServerTest {
	protected String baseURL;
	private AcServer server;
	private int serverPort;
	private File appFolder;

	@Before
	public void setUp() throws Exception {
		prepareServerConfiguration();
		startServer();

	}

	private void prepareServerConfiguration() throws IOException {
		appFolder = new File("appserver_tmp");
		cleanupConfiguration();
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
	}

	protected void startServer() throws SocketException {
		Config config = new Config("appserver_tmp/config.cfg");
		server = new AcServer(config);
		server.start();
		serverPort = server.getEndpoints().get(0).getAddress().getPort();
		baseURL = "localhost:"+serverPort+"/";
	}

	@After
	public void shutDownEndpoint() throws IOException, InterruptedException {
		stopServer();
		cleanupConfiguration();
	}

	private void cleanupConfiguration() throws IOException {
		delete(appFolder);
	}

	protected void stopServer() throws InterruptedException {
		server.stop();
		Thread.sleep(500);
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

	protected void installScript(String scriptName, String script) throws InterruptedException {
		Request newapp = Request.newPost();
		newapp.setURI(baseURL+"install?"+scriptName);
		newapp.setPayload(script);
		newapp.send();
		Response response = newapp.waitForResponse(100);
		assertEquals(CoAP.ResponseCode.CREATED, response.getCode());
		assertEquals("Application "+scriptName+" successfully installed to /install/"+scriptName, response.getPayloadString());
	}
}

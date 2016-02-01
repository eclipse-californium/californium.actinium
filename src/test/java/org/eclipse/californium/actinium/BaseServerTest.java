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
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BaseServerTest {
	public static final String TEST_APP_FOLDER = "appserver_tmp";
	public static final int TIMEOUT = 200;
	protected String baseURL;
	private AcServer server;
	private int serverPort;
	private File appFolder;

	@Before
	public void setUp() throws Exception {
		prepareServerConfiguration();
		startServer();
	}

	@After
	public void shutDownEndpoint() throws IOException, InterruptedException {
		stopServer();
		cleanupConfiguration();
	}

	private void prepareServerConfiguration() throws IOException {
		appFolder = new File(TEST_APP_FOLDER);
		cleanupConfiguration();
		appFolder.mkdir();
		appFolder.deleteOnExit();
		new File(TEST_APP_FOLDER + "/installed").mkdir();
		new File(TEST_APP_FOLDER + "/apps").mkdir();
		new File(TEST_APP_FOLDER + "/libs").mkdir();
		// source for installing scripts
		new File(TEST_APP_FOLDER + "/src").mkdir();
		{
			PrintWriter writer = new PrintWriter(TEST_APP_FOLDER + "/config.cfg", "UTF-8");
			writer.println("#Tue Sep 30 14:47:47 CEST 2014\n" +
					"apps_resource_id=apps\n" +
					"app_config_prefix=config_\n" +
					"app_config_path=" + TEST_APP_FOLDER + "/apps/\n" +
					"app_libs_path=" + TEST_APP_FOLDER + "/libs/\n" +
					"javascript_suffix=.js\n" + "app_path=" + TEST_APP_FOLDER +
					"/installed/\n" + "app_config_suffix=.cfg\n" +
					"install_resource_id=install\n" +
					"config_resource_id=config\n" +
					"app_config_resources=instances\n" +
					"running_resource_id=running\n" +
					"stats_resource_id=stats\n" +
					"start_on_install=true");
			writer.close();
		}
		{
			PrintWriter writer = new PrintWriter(TEST_APP_FOLDER + "/src/test_lib.js", "UTF-8");
			writer.println("exports.data = 20;\n" +
					"exports.getID = function() {\n" +
					"    return module.id;\n" +
					"};");
			writer.close();
		}
		{
			PrintWriter writer = new PrintWriter(TEST_APP_FOLDER + "/src/test_module.js", "UTF-8");
			writer.println("var test = require('test_lib');\n" +
					"app.root.onget = function(request) {\n" +
					"    request.respond(2.05, test.getID());\n" +
					"};");
			writer.close();
		}
	}

	protected void startServer() throws SocketException {
		Config config = new Config(TEST_APP_FOLDER + "/config.cfg");
		server = new AcServer(config);
		server.start();
		serverPort = server.getEndpoints().get(0).getAddress().getPort();
		baseURL = "localhost:" + serverPort + "/";
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
			throw new IOException("Cannot delete " + f);

	}

	protected void installScript(String scriptName, File scriptFile) throws InterruptedException, FileNotFoundException {
		String script = Utils.readFile(scriptFile);
		installScript(scriptName, script);
	}

	protected void installScript(String scriptName, String script) throws InterruptedException {
		installScript(scriptName, script, true, "install");
	}

	protected void installLibrary(String name, File libFile) throws InterruptedException, FileNotFoundException {
		String script = Utils.readFile(libFile);
		installLibrary(name, script);
	}

	protected void installLibrary(String name, String script) throws InterruptedException {
		installScript(name, script, true, "libs");
	}

	protected boolean installScript(String scriptName, String script, boolean check, String ep) throws InterruptedException {
		Request newapp = Request.newPost();
		newapp.setURI(baseURL + ep + "?" + scriptName);
		newapp.setPayload(script);
		newapp.send();
		Response response = newapp.waitForResponse(TIMEOUT);
		if (check) {
			assertEquals(CoAP.ResponseCode.CREATED, response.getCode());
			assertTrue(response.getPayloadString().endsWith(scriptName + " successfully installed to /" + ep + "/" + scriptName));
		} else {
			if (CoAP.ResponseCode.CREATED != response.getCode()) {
				return false;
			}
		}
		Request getapps = Request.newGet();
		getapps.setURI(baseURL + ep + "/");
		getapps.send();
		Response responseApps = getapps.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.CONTENT, responseApps.getCode());
		String payloadString = responseApps.getPayloadString();
		assertTrue(scriptName + " is installed", stringContainsLine(payloadString, scriptName));
		return true;
	}

	private boolean stringContainsLine(String string, String line) {
		return Arrays.asList(string.split("\\n")).contains(line);
	}

	protected void createInstance(String scriptName, String instanceName) throws InterruptedException {
		createInstance(scriptName, instanceName, true);
	}

	protected boolean createInstance(String scriptName, String instanceName, boolean check) throws InterruptedException {
		Request installApp = Request.newPost();
		installApp.setURI(baseURL + "install/" + scriptName);
		installApp.setPayload("name=" + instanceName);
		installApp.send();
		Response responseInstallApp = installApp.waitForResponse(TIMEOUT);
		if (check) {
			assertEquals(CoAP.ResponseCode.CREATED, responseInstallApp.getCode());
			assertEquals("Application " + scriptName + " successfully installed to /apps/running/" + instanceName, responseInstallApp.getPayloadString());
		}
		return CoAP.ResponseCode.CREATED == responseInstallApp.getCode();
	}

	protected void testCheckIfInstanceExists(final String instanceName) throws InterruptedException {
		String content = getInstances();
		assertTrue("Response contains \"" + instanceName + "\"", content.contains(instanceName));
	}

	protected void testCheckIfInstanceDoesNotExist(final String instanceName) throws InterruptedException {
		String content = getInstances();
		assertFalse("Response contains \"" + instanceName + "\"", content.contains(instanceName));
	}

	private String getInstances() throws InterruptedException {
		Request getapps2 = Request.newGet();
		getapps2.setURI(baseURL + "apps/instances");
		getapps2.send();
		Response runningApps = getapps2.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.CONTENT, runningApps.getCode());
		return runningApps.getPayloadString();
	}

	protected void testCheckInstance(final String scriptName, final String instanceName) throws InterruptedException {
		Request getapps2 = Request.newGet();
		getapps2.setURI(baseURL + "apps/instances/" + instanceName);
		getapps2.send();
		Response runningApps = getapps2.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.CONTENT, runningApps.getCode());
		String content = runningApps.getPayloadString();
		assertTrue("Response contains \"name: " + instanceName + "\"", content.contains("name: " + instanceName));
		assertTrue("Response contains \"app: " + scriptName + "\"", content.contains("app: " + scriptName));
	}

	protected void testCheckIfInstanceIsRunning(final String instanceName) throws InterruptedException {
		Request getapps2 = Request.newGet();
		getapps2.setURI(baseURL + "apps/running");
		getapps2.send();
		Response runningApps = getapps2.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.CONTENT, runningApps.getCode());
		String payloadString = runningApps.getPayloadString();
		assertTrue(instanceName + " is RUNNING", stringContainsLine(payloadString, instanceName));
	}

	public void testInstallHelloWorld(String scriptName) throws InterruptedException {
		testInstallHelloWorld(scriptName, true);
	}

	public boolean testInstallHelloWorld(String scriptName, boolean check) throws InterruptedException {
		String script = "app.root.onget = function(request) {\n" + "                  request.respond(ResponseCode.CONTENT, \"Hello World\");\n" + "              }";
		return installScript(scriptName, script, check, "install");
	}

	protected void testGET(String path, String expectedPayload) throws InterruptedException {
		Request request = Request.newGet();
		request.setURI(baseURL + path);
		request.send();
		Response response = request.waitForResponse(TIMEOUT);
		assertEquals(CoAP.ResponseCode.CONTENT, response.getCode());
		assertEquals(expectedPayload, response.getPayloadString());
	}
}

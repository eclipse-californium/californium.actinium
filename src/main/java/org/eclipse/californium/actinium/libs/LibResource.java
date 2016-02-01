package org.eclipse.californium.actinium.libs;

import org.eclipse.californium.actinium.AppManager;
import org.eclipse.californium.actinium.Utils;
import org.eclipse.californium.actinium.cfg.Config;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ynh on 22/10/15.
 */
public class LibResource extends CoapResource {
	private final AppManager manager;
	private final String name;

	public LibResource(String name, final AppManager manager) {
		super(name);
		this.name = name;
		this.manager = manager;
		setObservable(true);
	}

	public LibResource(String name, String payload, AppManager manager) {
		this(name, manager);
		storeLib(payload);
	}

	/**
	 * Responds the content of the app.
	 */
	@Override
	public void handleGET(CoapExchange request) {
		String content = Utils.readFile(getInstalledPath());
		if (content != null) {
			request.respond(ResponseCode.CONTENT, content);
		} else {
			request.respond(ResponseCode.NOT_FOUND);
		}
	}

	/**
	 * Deletes this app and all instances of it from the server. The AppManager
	 * stops the execution of all instances of this app and removes all config
	 * files from their parent resources and from the disk.
	 */
	@Override
	public void handleDELETE(CoapExchange request) {
		try {
			deleteLib(); // throws IOException if not successful (e.g. no
							// write-access to javascript file)
			delete();
			request.respond(CoAP.ResponseCode.DELETED);
		} catch (IOException e) {
			e.printStackTrace();
			request.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Deletes the file with the code of this lib.
	 * 
	 * @throws IOException
	 *             if file does not exist or is not deletable.
	 */
	private void deleteLib() throws IOException {
		String apppath = getInstalledPath();
		File file = new File(apppath);
		if (!file.exists())
			throw new IOException("The file " + apppath + " of app " + name + " doesn't exist on the disk");

		if (!file.canWrite())
			throw new IOException("The file " + apppath + " of app " + name + " is not writable/deletable");

		System.out.println("Delete app " + apppath);
		boolean success = file.delete();

		if (!success)
			throw new IOException("The file " + apppath + " of app " + name + " couldn't be deleted. Make sure, no other process is accessing it");
	}

	/**
	 * Updates the app. On an update, all instances of this app will be
	 * restarted.
	 */
	@Override
	public void handlePUT(CoapExchange request) {
		try {
			// update
			String code = request.getRequestText();
			storeLib(code);

			// restart all instances
			manager.restartAppsByLibraryName(name);

			changed();
			request.respond(CoAP.ResponseCode.CHANGED);

		} catch (Exception e) { // should not happen
			e.printStackTrace();
			request.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Store the given code to disk. This happens, when a new app is sent to the
	 * InstallResource or when an app is updated with new code.
	 *
	 * @param code
	 *            the code to store to the disk.
	 */
	private void storeLib(String code) {
		FileWriter appfile = null;
		try {
			String apppath = getInstalledPath();
			appfile = new FileWriter(apppath);
			appfile.write(code);

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Internal error while trying to store app to disk. IOException: " + e.getMessage(), e);
		} finally {
			try {
				if (appfile != null)
					appfile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Returns the path to the file with the code of this app.
	 * 
	 * @return the path to the file with the code of this app.
	 */
	private String getInstalledPath() {
		return manager.getConfig().getProperty(Config.APP_LIBS_PATH) + name + manager.getConfig().getProperty(Config.JAVASCRIPT_SUFFIX);
	}

}

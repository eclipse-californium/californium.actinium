package org.eclipse.californium.actinium.libs;

import org.eclipse.californium.actinium.AppManager;
import org.eclipse.californium.actinium.cfg.Config;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by ynh on 22/10/15.
 */
public class LibsResource extends CoapResource {
	private final AppManager manager;

	public LibsResource(AppManager manager) {
		super("libs");

		this.manager = manager;

		ArrayList<String> installed = getInstalledLibsName();
		for (String inst : installed) {
			LibResource res = new LibResource(inst, manager);
			add(res);
		}
	}

	/**
	 * Responds with a list of all installed apps.
	 */
	@Override
	public void handleGET(CoapExchange request) {
		StringBuffer buffer = new StringBuffer();

		// list all apps
		ArrayList<String> installed = getInstalledLibsName();
		for (String inst : installed) {
			buffer.append(inst + "\n");
		}

		request.respond(CoAP.ResponseCode.CONTENT, buffer.toString());
	}

	/**
	 * A POST request installs a new library. It retrieves the lib's name from
	 * the query. Thus the URL of the POST requests must be of the form
	 * [host]/libs?[libname]. InstallResource takes the request's content as
	 * code.
	 * <p>
	 * If the specified name for the app is valid, i.e. not already in use and
	 * not empty, InstallResource creates a new file on the disk and stores the
	 * request's content to it. Otherwise it responds with code
	 * RESP_BAD_REQUEST.
	 * <p>
	 * InstallResource then creates a new subresource of type
	 * InstalledAppResource, which represents the insalled app. It also stores
	 * the subresource's location in the location headder of the response
	 */
	@Override
	public void handlePOST(CoapExchange request) {
		System.out.println("Installer received data");
		try {
			// Figure out, whether payload is String or byte[] and install
			String payload = request.getRequestText();
			String query = request.getRequestOptions().getUriQueryString();

			// Throws IllegaArgumentException if request is not legal
			String newpath = installLibFromString(payload, query);

			Response response = new Response(CoAP.ResponseCode.CREATED);
			response.setPayload("Library " + query + " successfully installed to " + newpath);

			// Inform client about the location of the new resource
			response.getOptions().setLocationPath(newpath);

			request.respond(response);

		} catch (IllegalArgumentException e) { // given query invalid
			System.err.println(e.getMessage());
			request.respond(CoAP.ResponseCode.BAD_REQUEST, e.getMessage()); // RESP_PRECONDITION_FAILED?

		} catch (RuntimeException e) { // some error while processing (e.g. IO)
			e.printStackTrace();
			request.respond(CoAP.ResponseCode.BAD_REQUEST, e.getMessage()); // RESP_PRECONDITION_FAILED?

		} catch (Exception e) { // should not happen
			e.printStackTrace();
			request.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	private String installLibFromString(String payload, String name) {
		System.out.println("install library " + name);

		if (name == null)
			throw new IllegalArgumentException("The given library name is null. Please specify a valid name in the uri query");
		if (!name.matches("^[a-zA-Z0-9-_]*$")) {
			throw new IllegalArgumentException("The name may only contain alpha-numeric characters, dashes and underscores.");
		}
		if (name.length() == 0) {
			throw new IllegalArgumentException("The name can not be empty");
		}
		if (!isUnreservedName(name))
			throw new IllegalArgumentException("The given library name " + name + " is already in use. " + "Choose another name or update the current library with a PUT request");

		// we have a valid name, store program to disk
		LibResource res = new LibResource(name, payload, manager);
		add(res);

		return res.getURI();
	}

	private boolean isUnreservedName(String name) {
		String filename = getLibPath(name);
		return !new File(filename).exists() && !new File(manager.getConfig().getProperty(Config.APP_LIBS_PATH) + name).exists();
	}

	private String getLibPath(String name) {
		String path = manager.getConfig().getProperty(Config.APP_LIBS_PATH);
		return path + name + manager.getConfig().getProperty(Config.JAVASCRIPT_SUFFIX);
	}

	private ArrayList<String> getInstalledLibsName() {
		String path = manager.getConfig().getProperty(Config.APP_LIBS_PATH);
		String[] files = new File(path).list();
		ArrayList<String> ret = new ArrayList<>();
		if (files == null)
			return ret;
		for (String file : files) {
			int last = file.lastIndexOf('.');
			if (last >= 0) {
				ret.add(file.substring(0, last));
			}
		}
		return ret;
	}
}

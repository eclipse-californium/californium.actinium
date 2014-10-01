/*******************************************************************************
 * Copyright (c) 2014 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Martin Lanter - architect and initial implementation
 ******************************************************************************/
package org.eclipse.californium.actinium;

import java.net.SocketException;

import org.eclipse.californium.actinium.cfg.Config;
import org.eclipse.californium.actinium.install.InstallResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * Actinium (Ac) App-server for Californium
 * <p>
 * An Actinium app-server creates a structure of CoAP resources to run arbitrary
 * JavaScript apps on it. Most Notably an InstallResource to install new apps
 * and an AppResource which holds the instances of those apps.
 * <p>
 * An app or its subresources must not block the receiver thread of Californium.
 * Therefore every app has its own thread for handling requests. If a requests
 * has an app or a subresource of an app as target the server passes the request
 * to this app, whose receiver thread then will handle the request.
 */
public class AcServer extends CoapServer {

	// appserver's configuration
	private Config config;

	// AppManager to figure out to which apps a request belongs
	private AppManager manager;
	
	// resource that holds the stats for all app instances
	private StatsResource stats;
	
	/**
	 * Constructs a new Actinium app-server with the specified config.
	 * @param config the app server's config.
	 * @throws SocketException if the Socket is blocked.
	 */
	public AcServer(Config config) throws SocketException {
		
		//Log.setLevel(Level.ALL);
		this.config = config;
		
		this.manager = new AppManager(config);
		
		AppResource appres = new AppResource(manager);
		InstallResource insres = new InstallResource(manager);

		this.add(appres);
		this.add(insres);

		this.add(
				config.createConfigResource(config.getProperty(Config.CONFIG_RESOURCE_ID)));
		
		this.stats = new StatsResource(config, manager);
		this.add(stats);
		appres.startApps();
	}
	
	/**
	 * Setups a new config and a new app server.
	 * @param args no arguments required
	 */
	public static void main(String[] args) {
		try {
			
			Config config = new Config();
			AcServer server = new AcServer(config);
			server.start();
			
			System.out.println("Actinium (Ac) App-server listening on port "+server.getEndpoints().get(0).getAddress().getPort());
			
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
}

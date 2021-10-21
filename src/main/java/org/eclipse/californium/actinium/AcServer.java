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
 *    Matthias Kovatsch - creator and main architect
 *    Martin Lanter - architect and initial implementation
 ******************************************************************************/
package org.eclipse.californium.actinium;

import java.io.File;
import java.net.SocketException;

import org.eclipse.californium.actinium.cfg.Config;
import org.eclipse.californium.actinium.install.InstallResource;
import org.eclipse.californium.actinium.libs.LibsResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.elements.config.Configuration.DefinitionsProvider;
import org.eclipse.californium.elements.config.UdpConfig;
import org.eclipse.californium.core.config.CoapConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger LOG = LoggerFactory.getLogger(AcServer.class);

	private static DefinitionsProvider DEFAULTS = new DefinitionsProvider() {

		@Override
		public void applyDefinitions(Configuration config) {
			final int CORES = Runtime.getRuntime().availableProcessors();
			// javascripts are too frequently blocking!
			config.set(CoapConfig.PROTOCOL_STAGE_THREAD_COUNT, Math.max(8, CORES));
		}
	};

	// AppManager to figure out to which apps a request belongs
	private AppManager manager;

	// resource that holds the stats for all app instances
	private StatsResource stats;

	/**
	 * Constructs a new Actinium app-server with the specified config.
	 * 
	 * @param config the app server's config.
	 * @throws SocketException if the Socket is blocked.
	 */
	public AcServer(Config config) throws SocketException {
		this(config, null, null);
	}

	public AcServer(Config config, Configuration networkConfig, int... ports) throws SocketException {
		super(networkConfig, ports);

		this.manager = new AppManager(config);

		AppResource appres = new AppResource(manager);
		InstallResource insres = new InstallResource(manager);

		this.add(appres);
		this.add(insres);

		LibsResource libsres = new LibsResource(manager);
		this.add(libsres);

		this.add(config.createConfigResource(config.getProperty(Config.CONFIG_RESOURCE_ID)));

		this.stats = new StatsResource(config, manager);
		this.add(stats);
		appres.startApps();
	}

	@Override
	public void destroy() {
		manager.stopAllApps();
		super.destroy();
	}

	public static Configuration initConfiguration() {
		CoapConfig.register();
		UdpConfig.register();
		return Configuration.createWithFile(new File(Configuration.DEFAULT_FILE_NAME), Configuration.DEFAULT_HEADER, DEFAULTS);
	}

	/**
	 * Setups a new config and a new app server.
	 * 
	 * @param args no arguments required
	 */
	public static void main(String[] args) {
		try {
			initConfiguration();
			Config config = new Config();
			AcServer server = new AcServer(config);
			server.start();

			LOG.info("Actinium (Ac) App-server listening on port {}",
					server.getEndpoints().get(0).getAddress().getPort());

		} catch (SocketException e) {
			LOG.error("error starting Actinium App-server", e);
		}
	}
}

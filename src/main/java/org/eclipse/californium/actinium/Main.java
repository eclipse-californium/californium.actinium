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


/**
 * Calls the main Method of the AppServer.
 * Main entry points in the app server:
 * 	- ch.ethz.inf.vs.actinium.AcServer.main(): Creates a new AppServer
 *  - ch.ethz.inf.vs.actinium.AcShell.main(): Starts a console based app server with only one app running
 */
public class Main {

	public static void main(String[] args) {
		AcServer.main(args);
//		AcShell.main(args);
	}
}

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
package org.eclipse.californium.actinium.plugnplay;


/**
 * PlugAndPlayable is the interface for all apps. Apps must be able to start,
 * shutdown, restart and have a name.
 */
public interface PlugAndPlayable extends /* TODO: RequestHandler,*/ Runnable {

	// start the application in a new thread
	public void start();
	
	// shut the application down
	public void shutdown();
	
	// restart the application
	public void restart();
	
	// return the name of this application
	public String getName();
	
}

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
package org.eclipse.californium.actinium.cfg;

/**
 * This is a class for that the endities can be used as String without any
 * further computation (in contrary to an enum). It is also easy to introduce
 * new types, which only are not visible here.
 */
public final class AppType {

	// prevent instantiation
	private AppType() {}
	
	public static final String JAVASCRIPT = "javascript";
	public static final String UNKNOWN = "unknown";
	
	public static String getAppSuffix(String type) {
		if (JAVASCRIPT.equalsIgnoreCase(type))
			return "js";
		else
			throw new IllegalArgumentException("Unknown app type "+type);
	}
}

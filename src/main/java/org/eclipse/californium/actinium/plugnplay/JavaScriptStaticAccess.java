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
package org.eclipse.californium.actinium.plugnplay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaScriptStatisAccess defines global functions for JavaScript.
 */
public class JavaScriptStaticAccess {

	private static final Logger LOG = LoggerFactory.getLogger(JavaScriptStaticAccess.class);

	public static void dump(Object... args) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (i == 0)
				b.append("\tapp: ");
			else
				b.append(" ");
			b.append(args[i].toString());
		}
		LOG.debug(b.toString());
	}
}

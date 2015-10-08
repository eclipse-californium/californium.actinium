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
package org.eclipse.californium.actinium.jscoap.jserror;


public class NetworkErrorException extends RequestErrorException {

	private static final long serialVersionUID = -1781934300982074011L;

	public NetworkErrorException() {
		super();
	}

	public NetworkErrorException(String message) {
		super(message);
	}

	public NetworkErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public NetworkErrorException(Throwable cause) {
		super(cause);
	}
	
}

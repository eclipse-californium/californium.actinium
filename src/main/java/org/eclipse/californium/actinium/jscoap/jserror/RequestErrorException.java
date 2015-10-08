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

public class RequestErrorException extends RuntimeException {

	private static final long serialVersionUID = 4119685426310115359L;

	public RequestErrorException() {
		super();
	}

	public RequestErrorException(String message) {
		super(message);
	}

	public RequestErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public RequestErrorException(Throwable cause) {
		super(cause);
	}
	
}

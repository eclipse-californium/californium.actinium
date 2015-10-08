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

public class TimeoutErrorException extends RequestErrorException {

	private static final long serialVersionUID = 8201481582666993805L;

	public TimeoutErrorException() {
		super();
	}

	public TimeoutErrorException(String message) {
		super(message);
	}

	public TimeoutErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeoutErrorException(Throwable cause) {
		super(cause);
	}
	
}

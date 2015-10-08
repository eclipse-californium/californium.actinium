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

public class AbortErrorException extends RequestErrorException {

	private static final long serialVersionUID = -2833273004174819723L;

	public AbortErrorException() {
		super();
	}

	public AbortErrorException(String message) {
		super(message);
	}

	public AbortErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public AbortErrorException(Throwable cause) {
		super(cause);
	}
	
}

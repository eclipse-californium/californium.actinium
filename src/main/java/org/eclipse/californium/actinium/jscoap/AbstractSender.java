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
package org.eclipse.californium.actinium.jscoap;

import jdk.nashorn.internal.runtime.ScriptFunction;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;

/**
 * AbstractSender provides methods for calling JavaScript functions (listeners).
 */
public abstract class AbstractSender implements Sender {

	@Override
	public abstract void send(Request request);
	
	@Override
	public abstract void abort();

	
	/**
	 * Returns true, if the specified response only is an empty acknowledgement
	 * @param response the response
	 * @return true, if the specified response only is an empty acknowledgement
	 */
	protected boolean isAcknowledgement(Response response) {
		return response.getType() == Type.ACK && response.getPayloadSize() == 0;
	}
}

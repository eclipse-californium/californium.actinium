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

import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * AbstractSender provides methods for calling JavaScript functions (listeners).
 */
public abstract class AbstractSender implements Sender {

	@Override
	public abstract void send(Request request);
	
	@Override
	public abstract void abort();
	
	/**
	 * Calls the specified JavaScript function on the specified this object with
	 * the specified arguments
	 * 
	 * @param function the JavaScript function
	 * @param thisobj the this object
	 * @param args the parameters
	 */
	protected static void callJavaScriptFunction(Function function, CoapRequest thisobj, Object... args) {
		if (function!=null) {
			try {
				Context cx = Context.enter();
				Scriptable scope = function.getParentScope();
				function.call(cx, scope, Context.toObject(thisobj, scope), args);
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				Context.exit();
			}
		}
	}
	
	/**
	 * Returns true, if the specified response only is an empty acknowledgement
	 * @param response the response
	 * @return true, if the specified response only is an empty acknowledgement
	 */
	protected boolean isAcknowledgement(Response response) {
		return response.getType() == Type.ACK && response.getPayloadSize() == 0;
	}
}

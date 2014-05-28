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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import ch.ethz.inf.vs.californium.coap.DELETERequest;
import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.POSTRequest;
import ch.ethz.inf.vs.californium.coap.PUTRequest;
import ch.ethz.inf.vs.californium.endpoint.LocalResource;

/**
 * It is not possible to add further methods or fields to this class within
 * JavaScript (Rhino). If this is necessary, use AbstractJavaScriptResource.
 */
public class JavaScriptResource extends LocalResource implements CoAPConstants {
	// Cannot extend ScriptableObject, because has to extend LocalResource
	// Cannot (reasonably) implement Scriptable, because we then have to implement all 16 methods like ScriptableObject

	public Function onget = null;
	public Function onpost = null;
	public Function onput = null;
	public Function ondelete = null;
	
	public JavaScriptResource() {
		super(null);
	}

	public JavaScriptResource(String resourceIdentifier) {
		super(resourceIdentifier);
	}
	
	public JavaScriptResource(String resourceIdentifier, boolean hidden) {
		super(resourceIdentifier, hidden);
	}
	
	@Override
	public void changed() {
		super.changed();
	}
	
	public Function getOnget() {
		return onget;
	}
	
	public Function getOnpost() {
		return onpost;
	}
	
	public Function getOnput() {
		return onput;
	}
	
	public Function getOndelete() {
		return ondelete;
	}
	
	public Object getThis() {
		return this;
	}
	
	@Override
	public void performGET(GETRequest request) {
		Function onget = getOnget();
		if (onget!=null) {
			performFunction(onget, new JavaScriptCoAPRequest(request));
		} else {
			super.performGET(request);
		}
	}

	@Override
	public void performPOST(POSTRequest request) {
		Function onpost = getOnpost();
		if (onpost!=null) {
			performFunction(onpost, new JavaScriptCoAPRequest(request));
		} else {
			super.performPOST(request);
		}
	}

	@Override
	public void performPUT(PUTRequest request) {
		Function onput = getOnput();
		if (onput!=null) {
			performFunction(onput, new JavaScriptCoAPRequest(request));
		} else {
			super.performPUT(request);
		}
	}

	@Override
	public void performDELETE(DELETERequest request) {
		Function ondelete = getOndelete();
		if (ondelete!=null) {
			performFunction(ondelete, new JavaScriptCoAPRequest(request));
		} else {
			super.performDELETE(request);
		}
	}
	
	private void performFunction(Function fun, JavaScriptCoAPRequest request) {
//		NativeFunction fun = (NativeFunction) object;
		try {
			Context cx = Context.enter();
			Scriptable prototype = ScriptableObject.getClassPrototype(fun, request.getClassName());
			request.setPrototype(prototype);
			Scriptable scope = fun.getParentScope();
			Object thisObj = getThis();
			fun.call(cx, fun, Context.toObject(thisObj, scope), new Object[] {request});
		} finally {
			Context.exit();
		}
	}
}

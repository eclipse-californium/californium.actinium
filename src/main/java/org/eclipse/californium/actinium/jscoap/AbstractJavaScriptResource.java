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

/**
 * If an object in JavaScript (Rhino) is of type AbstractJavaScriptResource, it
 * is possible to add further methods and fields in JavaScript. However, in this
 * case it is not possible to use any other than the empty constructor. If the
 * constructor Resource(String) is required, use JavaScriptResource.
 */
public abstract class AbstractJavaScriptResource extends JavaScriptResource {

	/**
	 * It is not possible to have any other than the empty constructor in an
	 * abstract class in JavaScript (Rhino). Do not add any other constructors.
	 */
	public AbstractJavaScriptResource() {
		super(null);
	}

}

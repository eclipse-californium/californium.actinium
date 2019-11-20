/**
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */


package org.eclipse.californium.actinium;

import org.eclipse.californium.core.CoapResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A base class for implementing CoAP resources which provides support for
 * logging via SLF4J.
 *
 */
public abstract class LoggerProvidingResource extends CoapResource {

	/**
	 * The logger instance to be used by subclasses.
	 */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Creates a new resource.
	 * 
	 * @param name The resource name.
	 */
	public LoggerProvidingResource(String name) {
		super(name);
	}

	/**
	 * Creates a new resource.
	 * 
	 * @param name The resource name.
	 * @param visible {@code true} if the resource should be discoverable.
	 */
	public LoggerProvidingResource(String name, boolean visible) {
		super(name, visible);
	}

}

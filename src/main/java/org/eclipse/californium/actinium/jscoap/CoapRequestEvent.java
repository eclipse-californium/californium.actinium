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
 *    Yassin N. Hassan - initial implementation
 ******************************************************************************/
package org.eclipse.californium.actinium.jscoap;

import org.eclipse.californium.core.coap.Response;

/**
 * Created by ynh on 24/10/15.
 */
@FunctionalInterface
public interface CoapRequestEvent {
    void call(CoapRequest request, Response respose);
}

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
package org.eclipse.californium.actinium;

import ch.ethz.inf.vs.californium.coap.CodeRegistry;
import ch.ethz.inf.vs.californium.coap.POSTRequest;
import ch.ethz.inf.vs.californium.endpoint.LocalResource;

public class SlowResource extends LocalResource {

	private int counter = 0;
	
	public SlowResource() {
		super("slow");
	}
	
	@Override
	public void performPOST(POSTRequest request) {
		
		try {
			System.out.println("Bearbeite request "+request.getMID());
			Thread.sleep(5000);
			
			request.respond(CodeRegistry.RESP_CONTENT, "counter = "+counter);
			counter++;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

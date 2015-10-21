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
package org.eclipse.californium.actinium.plugnplay;

/**
 * JavaScriptStatisAccess defines global functions for JavaScript.
 */
public class JavaScriptStaticAccess {

	public static void dump(Object... args) {
        for (int i=0;i<args.length;i++) {
            if (i==0)
            	System.out.print("	app: ");
            else
                System.out.print(" ");
            System.out.print(args[i].toString());
        }
        System.out.println();
    }
//
//    public static void addSubResource(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
//    	if (args.length<2)
//    		throw new IllegalArgumentException("Invalid call to addSubResource. Must have the parent resource as first argument and the child resource as second");
//
//    	if ( !(args[0] instanceof Resource) || !(args[1] instanceof Resource))
//    		throw new IllegalArgumentException("Invalid call to addSubResource. The two arguments must be of type Resource");
//
//    	Resource parent = (Resource) Context.jsToJava(args[0],Resource.class);
//    	Resource child = (Resource) Context.jsToJava(args[1],Resource.class);
//    	parent.add(child);
//    }


}

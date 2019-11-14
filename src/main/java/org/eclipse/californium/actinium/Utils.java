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
 *    Matthias Kovatsch - creator and main architect
 ******************************************************************************/
package org.eclipse.californium.actinium;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by ynh on 02/11/15.
 */
public class Utils {

	public static String readFile(File file) {
		try {
			Scanner s = new Scanner(file);
			String contents = s.useDelimiter("\\Z").next();
			s.close();
			return contents;
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static String readFile(InputStream file) {
		Scanner s = new Scanner(file);
		String contents = s.useDelimiter("\\Z").next();
		s.close();
		return contents;
	}

	public static String readFile(URL resource) {
		try {
			return readFile(new File(resource.toURI()));
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public static String readFile(String path) {
		return readFile(new File(path));
	}
}

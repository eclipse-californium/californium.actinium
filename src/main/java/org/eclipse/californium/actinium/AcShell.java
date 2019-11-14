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
 *    Matthias Kovatsch - creator and main architect
 *    Martin Lanter - architect and initial implementation
 ******************************************************************************/
package org.eclipse.californium.actinium;

import java.io.File;
import java.net.SocketException;
import java.util.Observable;

import org.eclipse.californium.actinium.cfg.AppConfig;
import org.eclipse.californium.actinium.plugnplay.JavaScriptApp;
import org.eclipse.californium.core.CoapServer;

/**
 * A console based application that simulates an app server and runs a
 * JavaScriptApp specified in the arguments given in the main method.
 */
public class AcShell extends CoapServer {

	private static final int IDX_FILE = 0;

	public static final int ERR_INIT_FAILED = 1;
	public static final int ERR_NO_FILE_SPECIFIED = 2;
	public static final int ERR_FILE_IO = 3;
	
	private String path;
	private String name;
	
	public AcShell(String path, String name) throws SocketException {
		super();
		this.path = path;
		this.name = name;
	}
	
	public AcShell(String path, String name, int port) throws SocketException {
		super(port);
		this.path = path;
		this.name = name;
	}
	
	private void execute() {
		File file = new File(path);
	    String code = Utils.readFile(file);
	    
	    if (code==null) {
			System.err.println("File not found: " + path);
			System.exit(ERR_FILE_IO);
		}
	    if (name==null) {
	    	name = file.getName();
	    }
	    
	    AppConfig appcfg = new AppConfig();
	    appcfg.setProperty(AppConfig.NAME, name);
	    OneTimeJavaScriptApp app = new OneTimeJavaScriptApp(appcfg);
	    add(app);
	    
	    app.execute(code);
	}

	public static void main(String[] args) {
		if (args.length==0) {
			printInfo();
			System.exit(ERR_NO_FILE_SPECIFIED);
		}
		
		// parse arguments
		try {
			int index = 0; // expected argument
			
			int port = -1;
			String file = null;
			String name = null;
			
			for (int i=0;i<args.length;i++) {
				String arg = args[i]; // currenct arg
				if (arg.startsWith("-")) { // arg is an option
					if (arg.equals("-port")) {
						try {
							port = Integer.parseInt(args[i+1]);
						} catch (NumberFormatException e) {
							throw new IllegalArgumentException("Invalid port number "+args[i+1], e);
						}
						i = i+1; // skip next arg, which is the port number
					
					} else if (arg.equals("-name")) {
						name = args[i+1];
						i = i+1; // skip next arg, which is the name
					} else {
						System.err.println("Error: unrecognized or incomplete command line.");
						printInfo();
						System.exit(ERR_NO_FILE_SPECIFIED);
					}
					
				} else { // arg is a required argument
					if (index==IDX_FILE) {
						file = arg;
					}
					index++;
				}
			}
			
			if (file==null) {
				System.err.println("No JavaScript file to be launched specified");
				System.exit(ERR_NO_FILE_SPECIFIED);
			}
			
			// setup server and execute code
			AcShell server;
			if (port==-1) server = new AcShell(file, name);
			else server = new AcShell(file, name, port);
			
			server.start();
			System.out.println("Actinium (Ac) App-server listening on port " + server.getEndpoints().get(0).getAddress().getPort());
			server.execute();
			
		} catch (Exception e) {
			System.err.println("Failed to launch AcShell");
			e.printStackTrace();
			System.exit(ERR_INIT_FAILED);
		}
	}
	
	public static void printInfo() {
		System.out.println(
				"Actinium (Ac) Shell" +
				"\n" +
				"\nUsage: AcShell [-port] [-name] FILE" +
				"\n  FILE : The JavaScript app to be launched" +
				"\nOptions:" +
				"\n  -port: Listen on specified port (Default: 5683)" +
				"\n  -name: Name of the created Resource (Default: filename)"
			);
	}
	
	private static class OneTimeJavaScriptApp extends JavaScriptApp {
		
		private OneTimeJavaScriptApp(AppConfig appcfg) {
			super(null, appcfg);
		}
		
		@Override
		public void start() { throw new UnsupportedOperationException(); }
		
		@Override
		public void restart() { throw new UnsupportedOperationException(); }
		
		@Override
		public void shutdown() { System.exit(0); }
		
		@Override
		public void run() { throw new UnsupportedOperationException(); }
		
		@Override
		public void update(Observable o, Object arg) { throw new UnsupportedOperationException(); }
		
		@Override
		public void execute(String code) {
			super.execute(code);
		}
	}
}

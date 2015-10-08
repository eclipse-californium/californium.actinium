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
package org.eclipse.californium.actinium.cfg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.Properties;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * AbstractConfig is a Properties which also has tha capabilities of a CoAP
 * resource and an Observable.
 * <p>
 * On a GET request, it returns all properties specified in this configuration.
 * On a POST request, it changes the properties from the request's payload
 * accordingly if possible. On a PUT request, it replaces all properties with
 * the properties from the request's payload, where possible. DELETE requests
 * are not allowed if not implemented by a subclass.
 * <p>
 * Some properties cannot be changed from an extern object with
 * setProperties(...). These are the properties with the key for which the
 * method isModifiable(key) return false, which should be overriden by
 * subclasses.
 * <p>
 * To change one or more properties they must be sent by a POST request and the
 * payload must be of the form "[key] = [value]" for all properties to be
 * changed. E.g. to change the property "name" to the value "myname" send a POST
 * request with "name = myname" as payload.
 * <p>
 * If properties are changed through a POST request or a call to
 * setPropertiesAndNotify(...) AbstractConfig notifies all its observers. As
 * argument for their update method an instance of ConfigChangeSet is used,
 * which contains all keys, that have been changed.
 */
public abstract class AbstractConfig extends Properties {

	private static final long serialVersionUID = 1533763543185322735L;

	// Resource for this config must be created with an identifier
	private CoapResource cfgres = null;
	
	// The path, where this config shall be stored to if not mentioned otherwise
	private String configPath;
	
	private ConfigObvervable observable;
	
	public AbstractConfig() {
		super();
		this.cfgres = null;
		this.observable = new ConfigObvervable();
	}
	
	public AbstractConfig(String configPath) {
		this();
		this.configPath = configPath;
	}
	
	public CoapResource createConfigResource(String identifier) {
		this.cfgres = new ConfigResource(identifier);
		return cfgres;
	}
	
	public Resource getConfigResource() {
		return cfgres;
	}
	
	public Observable getObservable() {
		return observable;
	}
	
	protected void fireNotification(String... changes) {
		fireNotification(new ConfigChangeSet(changes));
	}
	
	protected void fireNotification(ConfigChangeSet changes) {
		observable.fireNotification(changes);
	}
	
	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}
	
	public void setPropertyAndNotify(String... props) {
		ConfigChangeSet changes = new ConfigChangeSet();
		for (int i=0;i<props.length-1;i+=2) {
			setProperty(props[i], props[i+1]);
			changes.add(props[i]);
		}
		fireNotification(changes);
	}
	
	public void setProperty(String key, int value) {
		setProperty(key, String.valueOf(value));
	}
	
	public void setProperty(String key, boolean value) {
		setProperty(key, String.valueOf(value));
	}
	
	public int getInt(String key) {
		String value = getProperty(key);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				System.err.println("Invalid integer property: "+key+"="+value);
			}
			
		} else {
			System.err.println("Undefined integer property: "+key);
		}
		return 0;
	}
	
	public boolean getBool(String key) {
		String value = getProperty(key);
		if (value != null) {
			try {
				return Boolean.parseBoolean(value);
			} catch (NumberFormatException e) {
				System.err.println("Invalid boolean property: "+key+"="+value);
			}
			
		} else {
			System.err.println("Undefined boolean property: "+key);
		}
		return false;
	}
	
	public void store() {
		System.out.println("Store config to file "+configPath);
		storeProperties(configPath);
	}
	
	public void deleteConfig() throws IOException {
		File file = new File(configPath);
		if (!file.exists())
			throw new IOException("The config file "+configPath+" doesn't exist on the disk");
		
		if (!file.canWrite())
			throw new IOException("The config file "+configPath+" is not writable/deletable");
		
		System.out.println("Delete config file "+configPath);
		boolean success = file.delete();
		
		if (!success)
			throw new IOException("The config file "+configPath+" couldn't be deleted. Make sure, no other process is accessing it");
		
		cfgres.delete();
	
	}
	
	protected void loadProperties(String path) {
		try {
			FileInputStream stream = new FileInputStream(path);
			try {
				load(stream);
			} finally {
				stream.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Property file "+path+" not found. Try to restore");
			storeProperties(path);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Unable to load properties from "+path);
		}
	}
	
	protected void storeProperties(String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path); 
			store(fos, null);
			fos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Unable to store properties at "+path);
		}
	}
	
	public boolean isModifiable(String key) {
		return true;
	}
	
	public void handleGET(CoapExchange request) {
		Response response = new Response(ResponseCode.CONTENT);

		StringBuffer buffer = new StringBuffer();
		buffer.append("App Server Configuration\n");
		for (String key:stringPropertyNames()) {
			buffer.append("	"+key+": "+get(key)+" ("+(isModifiable(key)?"modifiable":"unmodifiable")+")\n");
		}
		buffer.append("stored at "+getConfigPath()+"\n");
		
		response.setPayload(buffer.toString());
		request.respond(response);
	}

	public void handlePUT(CoapExchange request) {Properties p = new Properties();
		try {
			StringReader reader = new StringReader(request.getRequestText());
			p.load(reader);
			
			System.out.println("update config:");
			for (String key:p.stringPropertyNames()) 
				System.out.println("	"+key+" = >"+p.get(key)+"< "+isModifiable(key));
			
			for (String key:this.stringPropertyNames()) {
				if (isModifiable(key))
					remove(key);
			}

			ConfigChangeSet changes = new ConfigChangeSet();
			for (String key:p.stringPropertyNames()) {
				if (isModifiable(key)) {
					setProperty(key, p.getProperty(key));
					changes.add(key);
				}
			}
			store();
			fireNotification(changes);
			
			request.respond(ResponseCode.CHANGED, "successfully changed keys: "+changes);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Configuration was not able to be parsed");
			request.respond(ResponseCode.BAD_REQUEST, "Configuration was not able to be parsed");
		}
	}

	public void handlePOST(CoapExchange request) {
		Properties p = new Properties();
		try {
			StringReader reader = new StringReader(request.getRequestText());
			p.load(reader);
			
			System.out.println("update config:");
			for (String key:p.stringPropertyNames()) 
				System.out.println("	"+key+" = >"+p.get(key)+"< "+(isModifiable(key)?"modifiable":"unmodifiable"));

			ConfigChangeSet changes = new ConfigChangeSet();
			for (String key:p.stringPropertyNames()) {
				if (isModifiable(key)) {
					setProperty(key, p.getProperty(key));
					changes.add(key);
				}
			}
			store();
			fireNotification(changes);
			
			request.respond(ResponseCode.CHANGED, "successfully changed keys: "+changes);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Configuration was not able to be parsed");
			request.respond(ResponseCode.BAD_REQUEST, "Configuration was not able to be parsed");
		}
	}
	
	// must be here, for that subclasses can override it.
	public void handleDELETE(CoapExchange request) {
		request.respond(ResponseCode.METHOD_NOT_ALLOWED);
	}

	private class ConfigResource extends CoapResource {

		public ConfigResource(String identifier) {
			super(identifier);
		}
		
		@Override
		public void handleGET(CoapExchange request) {
			AbstractConfig.this.handleGET(request);
		}

		@Override
		public void handlePUT(CoapExchange request) {
			AbstractConfig.this.handlePUT(request);
		}

		@Override
		public void handlePOST(CoapExchange request) {
			AbstractConfig.this.handlePOST(request);
		}

		@Override
		public void handleDELETE(CoapExchange request) {
			AbstractConfig.this.handleDELETE(request);
		}
	}
	
	private class ConfigObvervable extends Observable {
		private void fireNotification(ConfigChangeSet changes) {
			setChanged();
			notifyObservers(changes);
		}
	}
	
	public static class ConfigChangeSet extends HashSet<String> {
		private static final long serialVersionUID = 3833165415676915271L;
		
		public ConfigChangeSet() {}
		public ConfigChangeSet(String... changes) {
			for (String str:changes)
				add(str);
		}
		public String toString() {
			return Arrays.toString(toArray());
		}
	}
}


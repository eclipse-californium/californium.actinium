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

import java.util.LinkedList;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

/**
 * Inspired by
 * http://www.ibm.com/developerworks/library/j-jtp0730/index.html
 */
public class WorkQueue {
	
	private final PoolWorker thread;
	private final LinkedList<Runnable> queue;

	public WorkQueue() {
		this(null);
	}
	
	public WorkQueue(String name) {
		queue = new LinkedList<Runnable>();
		if (name==null)
			thread = new PoolWorker();
		else
			thread = new PoolWorker(name);
		//thread.start();
	}

	public void deliver(CoapExchange request, CoapResource resource) {
		synchronized (queue) {
			queue.addLast(new RequestDelivery(request, resource));
			queue.notify(); // notifyAll not required
		}
	}
	
	public void deliver(Runnable runnable) {
		synchronized (queue) {
			queue.addLast(runnable);
			queue.notify(); // notifyAll not required
		}
	}
	
	/**
	 * Starts the queue concurrently
	 */
	public void start() {
		thread.start();
	}
	
	/**
	 * Executes the queue with the thread, that calls this method
	 */
	public void execute() {
		thread.run();
	}
	
	public void stop() {
		thread.interrupt();
	}

	/*
	 * The handler for the requests of the queue
	 */
	private class PoolWorker extends Thread {
		
		private PoolWorker() {
			super();
		}
		
		private PoolWorker(String name) {
			super(name);
		}
		
		public void run() {
			Runnable r;
			while (true) {
				// wait for another task to execute
				synchronized (queue) {
					while (queue.isEmpty()) {
						try {
							queue.wait();
						} catch (InterruptedException ignored) { }
					}
					r = queue.removeFirst();
					r.run();
				}
			}
		}
	}
	
	private class RequestDelivery implements Runnable {
		
		private CoapExchange request;
		private CoapResource resource;
		
		private RequestDelivery(CoapExchange request, CoapResource resource) {
			this.request = request;
			this.resource = resource;
		}
		
		public void run() {
			/*
			 * Calls performXXX Method. If an exception occurs it must be
			 * caught, to ensure, the thread doesn't stop.
			 */
			try {
				resource.handleRequest(request.advanced());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

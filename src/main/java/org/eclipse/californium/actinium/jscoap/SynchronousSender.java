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

import org.eclipse.californium.actinium.jscoap.jserror.AbortErrorException;
import org.eclipse.californium.actinium.jscoap.jserror.RequestErrorException;
import org.eclipse.californium.actinium.jscoap.jserror.TimeoutErrorException;
import org.eclipse.californium.core.coap.MessageObserverAdapter;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.mozilla.javascript.Function;

/**
 * SynchronousSender implements the process to send a request synchronously.
 * Conforms to the CoAPRequest API
 * (http://lantersoft.ch/download/bachelorthesis/CoAPRequest_API.pdf)
 */
public class SynchronousSender extends AbstractSender {

	private CoapRequest coapRequest;
	
	private Function onready; // onreadystatechange
	private Function ontimeout;
	private Function onload;
	private Function onerror;
	
	private long timeout;
	private long timestamp;
	
	private final Lock lock = new Lock(); // never sync(coapreq) within sync(lock)
	
	public SynchronousSender(CoapRequest coapRequest, Function onready, Function ontimeout, Function onload, Function onerror, long timeout) {
		this.coapRequest = coapRequest;
		this.onready = onready;
		this.ontimeout = ontimeout;
		this.onload = onload;
		this.onerror = onerror;
		this.timeout = timeout;
	}
	
	// by app's execution thread (must not be app's receiver thread)
	@Override
	public void send(Request request) {
		request.addMessageObserver(new MessageObserverAdapter() {
			@Override
			public void onResponse(Response response) {
				if (!isAcknowledgement(response)) {
					SynchronousSender.this.handleSyncResponse(response);
				}
			}
		});
		
		try {
			synchronized (lock) {
				if (!lock.aborted) { // if not already aborted
					timestamp = System.currentTimeMillis();
					
					request.send();
					
					if (timeout<=0) {
						waitForResponse();
					} else {
						waitForResponse(timeout);
					}
				}
			}
		} catch (InterruptedException e) {
			throw new RequestErrorException(e.getMessage());
		} catch (Exception e) {
			// TODO set error code
			handleError(onerror);
		}
	}
				
	private void waitForResponse() throws InterruptedException {
		while(!lock.receivedresponse && !lock.aborted) {
			lock.wait();
			checkAborted();
			checkTimeout();
		}
		checkAborted(); // check again for aborted in case the loop has not been entered
	}
	
	private void waitForResponse(long timeout) throws InterruptedException {
		while(!lock.receivedresponse && !lock.aborted) {
			long ttw = timestamp + timeout - System.currentTimeMillis(); // time to wait
			if (ttw<=0) checkTimeout();
			lock.wait(ttw); // ttw>0
			checkAborted();
			checkTimeout();
		}
		checkAborted(); // check again for aborted in case the loop has not been entered
	}
	
	// by app's ReceiverThread
	private void handleSyncResponse(Response response) {
		synchronized (lock) {
			lock.receivedresponse = true;
			
			if (!lock.aborted && !lock.timeouted) {
				coapRequest.setResponse(response);
				coapRequest.setReadyState(CoapRequest.DONE);
				
				/*
				 * While the app's receiver thread executes this function, the
				 * caller of send() is still blocked!
				 */
				callJavaScriptFunction(onready, coapRequest, response);
				callJavaScriptFunction(onload, coapRequest, response);
			}

			lock.notifyAll();
		}
	}
	
	@Override
	public void abort() {
		synchronized (lock) {
			lock.aborted = true;
			lock.notifyAll();
		}
	}
	
	/**
	 * Checks whether a timeout in a synchronized request has occured. If it
	 * has, it calls the JavaScript function ontimeout if defined and then
	 * throws a CoAPTimeoutException. throws a CoAPTimeoutException.
	 * <p>
	 * Must have monitor on lock.
	 * 
	 * @param timestamp milliseconds since 1970.
	 * @param timeout
	 */
	private void checkTimeout() {
		if (isTimeout(timestamp, timeout)) {
			lock.timeouted = true;

			// by app's execution thread (who has called send())
			handleError(ontimeout);
			
			throw new TimeoutErrorException("Timout ("+timeout+") ms");
		}
	}
	
	/**
	 * Checks whether a timeout has occured. 
	 * @param timestamp milliseconds since 1970.
	 * @param timeout
	 */
	private boolean isTimeout(long timestamp, long timeout) {
		long now = System.currentTimeMillis();
		return now >= timestamp + timeout && !lock.aborted && !lock.receivedresponse;
	}
	
	/**
	 * Checks whether the connection has been aborted. If so, throws a
	 * RuntimeException.
	 */
	private void checkAborted() {
		if (lock.aborted && !lock.receivedresponse && !lock.timeouted) {
			synchronized (coapRequest) {
				coapRequest.setError(true);
				coapRequest.setReadyState(CoapRequest.DONE);
				coapRequest.setSend(false);
			}
			callJavaScriptFunction(onready, coapRequest);
			coapRequest.setReadyState(CoapRequest.UNSENT);
			throw new AbortErrorException("Connection has been aborted");
		}
	}
	
	private void handleError(Function function) {
		synchronized (coapRequest) {
			coapRequest.setError(true);
			coapRequest.setReadyState(CoapRequest.DONE);
		}
		callJavaScriptFunction(onready, coapRequest);
		callJavaScriptFunction(function, coapRequest);
	}
	
	private class Lock {
		private boolean receivedresponse = false;
		private boolean aborted = false;
		private boolean timeouted = false;
	}
}

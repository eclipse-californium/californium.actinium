/*******************************************************************************
 * Copyright (c) 2014 Institute for Pervasive Computing, ETH Zurich and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * <p>
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.html.
 * <p>
 * Contributors:
 * Matthias Kovatsch - creator and main architect
 * Martin Lanter - architect and initial implementation
 ******************************************************************************/
package org.eclipse.californium.actinium.jscoap;

import org.eclipse.californium.actinium.jscoap.jserror.NetworkErrorException;
import org.eclipse.californium.core.coap.MessageObserverAdapter;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;

import java.util.Timer;
import java.util.TimerTask;

/**
 * AsynchronousSender implements the process to send a request asynchronously.
 * Conforms to the CoapRequest API
 * (http://lantersoft.ch/download/bachelorthesis/CoAPRequest_API.pdf)
 * <p>
 * Only one of the functions onload, ontimeout, onerror gets called (according
 * to the outcome of the request).
 */

public class AsynchronousSender extends AbstractSender {

    private final Lock lock = new Lock();
    private Timer timer = new Timer();
    private CoapRequest coapRequest;
    private CoapRequestEvent onready; // onreadystatechange
    private CoapRequestEvent ontimeout; // timeout error
    private CoapRequestEvent onload;
    private CoapRequestEvent onerror; // if a network error occurs
    private long timeout;

    public AsynchronousSender(CoapRequest coapRequest, CoapRequestEvent onready, CoapRequestEvent ontimeout, CoapRequestEvent onload, CoapRequestEvent onerror, long timeout) {
        this.coapRequest = coapRequest;
        this.onready = onready;
        this.ontimeout = ontimeout;
        this.onload = onload;
        this.onerror = onerror;
        this.timeout = timeout;
    }

    @Override
    public void send(Request request) {
        request.addMessageObserver(new MessageObserverAdapter() {
            @Override
            public void onResponse(Response response) {
                if (!isAcknowledgement(response)) {
                    handleAsyncResponse(response);
                }
            }
        });

        try {
            request.send();
        } catch (Exception e) {
            handleError(onerror);
            throw new NetworkErrorException(e.toString());
        }

        // TODO use TokenLayer timeout
        if (timeout > 0) {
            timer.schedule(new TimerTask() {
                public void run() { // this is always called in a new thread
                    boolean istimeout;
                    synchronized (lock) {
                        if (!lock.receivedresponse)
                            lock.timeouted = true;
                        istimeout = !lock.receivedresponse && !lock.aborted;
                    }
                    if (istimeout) {
                        handleError(ontimeout);
                    }
                }
            }, timeout);
        }
    }

    // by ReceiverThread
    private void handleAsyncResponse(Response response) {
        boolean callonready;
        synchronized (lock) {
            callonready = !lock.aborted && !lock.timeouted;
            if (callonready)
                lock.receivedresponse = true;
        }
        if (callonready) {
            synchronized (coapRequest) {
                coapRequest.setResponse(response);
                coapRequest.setReadyState(CoapRequest.DONE);
            }
            if (onready != null)
                onready.call(coapRequest, response);
            if (onload != null)
                onload.call(coapRequest, response);
        }
    }

    @Override
    public void abort() {
        boolean isabort;
        // Terminate send() algorithm
        synchronized (lock) {
            isabort = !lock.receivedresponse && !lock.timeouted;
            if (isabort)
                lock.aborted = true;
        }
        if (isabort) {
            synchronized (coapRequest) {
                coapRequest.setError(true);
                coapRequest.setReadyState(CoapRequest.DONE);
                coapRequest.setSend(false);
            }
            if (onready != null)
                onready.call(coapRequest, null);
            coapRequest.setReadyState(CoapRequest.UNSENT);
            // no onreadystatechange event is dispatched
        }
    }

    private void handleError(CoapRequestEvent function) {
        synchronized (coapRequest) {
            coapRequest.setError(true);
            coapRequest.setReadyState(CoapRequest.DONE);
        }
        if (onready != null)
            onready.call(coapRequest, null);
        if (function != null)
            function.call(coapRequest, null);
    }

    private class Lock {
        private boolean aborted = false;
        private boolean receivedresponse = false;
        private boolean timeouted = false;
    }

}

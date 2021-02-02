/*******************************************************************************
 * Copyright (c) 2014 Institute for Pervasive Computing, ETH Zurich and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * <p>
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.html.
 * <p>
 * Contributors:
 * Matthias Kovatsch - creator and main architect
 * Martin Lanter - architect and initial implementation
 ******************************************************************************/
package org.eclipse.californium.actinium.jscoap;

import org.eclipse.californium.actinium.jscoap.jserror.NetworkErrorException;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MessageObserverAdapter;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.elements.util.DaemonThreadFactory;
import org.eclipse.californium.elements.util.ExecutorsUtil;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sender implements the process to send a request.
 * Conforms to the CoapRequest API
 * (http://lantersoft.ch/download/bachelorthesis/CoAPRequest_API.pdf)
 * <p>
 * Only one of the functions onload, ontimeout, onerror gets called (according
 * to the outcome of the request).
 */

public class Sender {
    private static final ThreadGroup JSCOAP_THREAD_GROUP = new ThreadGroup("jscoap"); //$NON-NLS-1$
    private static final ScheduledExecutorService SCHEDULER;

    static {
        JSCOAP_THREAD_GROUP.setDaemon(false);
        SCHEDULER = ExecutorsUtil.newScheduledThreadPool(2, new DaemonThreadFactory("jstimer#", JSCOAP_THREAD_GROUP));
    }

    public static final int READY = 0;
    public static final int SENT = 1;
    public static final int TIMEOUT = 2;
    public static final int ABORTED = 3;
    public static final int DONE = 4;
    public static final int ERROR = 5;

    private final AtomicInteger state = new AtomicInteger(READY);
    private final CoapRequest coapRequest;
    private final CoapRequestEvent onready; // onreadystatechange
    private final CoapRequestEvent ontimeout;
    private final CoapRequestEvent onload;
    private final CoapRequestEvent onerror; // if a network error occurs
    private final long timeout;
    private final boolean async;
    private final Request request;

    public Sender(CoapRequest coapRequest, Request request, CoapRequestEvent onready, CoapRequestEvent ontimeout, CoapRequestEvent onload, CoapRequestEvent onerror, long timeout, boolean async) {
        this.coapRequest = coapRequest;
        this.onready = onready;
        this.ontimeout = ontimeout;
        this.onload = onload;
        this.onerror = onerror;
        this.timeout = timeout;
        this.async = async;
        this.request = request;
    }

    public void send() {
        if (state.compareAndSet(READY, SENT)) {
            if (async) {
                request.addMessageObserver(new MessageObserverAdapter() {
                    @Override
                    public void onResponse(Response response) {
                        if (!isAcknowledgement(response)) {
                            handleResponse(response);
                        }
                    }

                    @Override
                    public void onTimeout() {
                        handleError(TIMEOUT);
                    }
                });
            }
            try {
                request.send();
            } catch (Exception e) {
                handleError(ERROR);
                throw new NetworkErrorException(e.toString());
            }
            if (async) {
                if (timeout > 0) {
                    SCHEDULER.schedule(new Runnable() {
                        @Override
                        public void run() {
                            request.setTimedOut(true);
                        }
                    }, timeout, TimeUnit.MILLISECONDS);
                }
            } else {
                try {
                    Response result = request.waitForResponse(timeout);
                    if (result == null) {
                        request.setTimedOut(true);
                        handleError(TIMEOUT);
                    }else {
                        handleResponse(result);
                    }
                } catch (InterruptedException e) {
                    request.setTimedOut(true);
                    handleError(TIMEOUT);
                }
            }
        }
    }

    // by ReceiverThread
    private void handleResponse(Response response) {
        if (state.compareAndSet(SENT, DONE)) {
            coapRequest.setResponse(response);
            coapRequest.setReadyState(CoapRequest.DONE);
            if (onready != null)
                onready.call(coapRequest, response);
            if (onload != null)
                onload.call(coapRequest, response);
        }
    }


    public void abort() {
        if (state.compareAndSet(SENT, ABORTED)) {
            request.cancel();
            coapRequest.setError(true);
            coapRequest.setReadyState(CoapRequest.DONE);
            coapRequest.setSend(false);
            if (onready != null)
                onready.call(coapRequest, null);
            coapRequest.setReadyState(CoapRequest.UNSENT);
        }
    }

    private void handleError(int newState) {
        if (state.compareAndSet(SENT, newState)) {
            CoapRequestEvent function = newState == TIMEOUT ? ontimeout : onerror;
            coapRequest.setError(true);
            coapRequest.setReadyState(CoapRequest.DONE);
            if (onready != null)
                onready.call(coapRequest, null);
            if (function != null)
                function.call(coapRequest, null);
        }
    }

    protected boolean isAcknowledgement(Response response) {
        return response.getType() == CoAP.Type.ACK && response.getPayloadSize() == 0;
    }
}

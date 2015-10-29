package org.eclipse.californium.actinium.jscoap;


import org.eclipse.californium.core.coap.Request;

@FunctionalInterface
public interface CoapCallback {
    void call(JavaScriptCoapExchange ex, Request request);
}

package org.eclipse.californium.actinium.jscoap;


@FunctionalInterface
public interface CoapCallback {
    void call(JSCoapExchange ex);
}

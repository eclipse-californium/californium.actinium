package org.eclipse.californium.actinium.jscoap;

import org.eclipse.californium.core.server.resources.CoapExchange;


@FunctionalInterface
public interface CoapCallback {
    void call(CoapExchange ex);
}

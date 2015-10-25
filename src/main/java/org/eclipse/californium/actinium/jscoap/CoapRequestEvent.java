package org.eclipse.californium.actinium.jscoap;

import org.eclipse.californium.core.coap.Response;

/**
 * Created by ynh on 24/10/15.
 */
@FunctionalInterface
public interface CoapRequestEvent {
    void call(CoapRequest request, Response respose);
}

package org.eclipse.californium.actinium.jscoap;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.net.InetAddress;

/**
 * Created by ynh on 25/10/15.
 */
public class JavaScriptCoapExchange {
    private final CoapExchange exchange;

    public JavaScriptCoapExchange(CoapExchange exchange) {
        this.exchange = exchange;
    }

    public InetAddress getSourceAddress() {
        return this.exchange.getSourceAddress();
    }

    public int getSourcePort() {
        return this.exchange.getSourcePort();
    }

    public CoAP.Code getRequestCode() {
        return this.exchange.getRequestCode();
    }

    public OptionSet getRequestOptions() {
        return this.exchange.getRequestOptions();
    }

    public byte[] getRequestPayload() {
        return this.exchange.getRequestPayload();
    }

    public String getRequestText() {
        return this.exchange.getRequestText();
    }

    public void accept() {
        this.exchange.accept();
    }

    public void reject() {
        this.exchange.reject();
    }

    public void setLocationPath(String path) {
        this.exchange.setLocationPath(path);
    }

    public void setLocationQuery(String query) {
        this.exchange.setLocationQuery(query);
    }

    public void setMaxAge(long age) {
        this.exchange.setMaxAge(age);
    }

    public void setETag(byte[] tag) {
        this.exchange.setETag(tag);
    }

    public void respond(CoAP.ResponseCode code) {
        this.exchange.respond(code);
    }

    public void respond(Object jsCode){
        this.exchange.respond(getResponseCode(jsCode));
    }

    private CoAP.ResponseCode getResponseCode(Object jsCode) {
        Integer code;

        // Parse code (e.g. 69, 2.05 or "Content")
        if (jsCode instanceof Integer) {
            code = (Integer) jsCode;
            // Problem: 4.00 becomes the Integer 4 -> convert 1--5 to actual raw value code
            if (code <= 5) {
                code *= 32;
            }
        } else if (jsCode instanceof String) {
            code = CoAPConstantsConverter.convertStringToCode((String) jsCode);
        } else if (jsCode instanceof Double) {
            code = CoAPConstantsConverter.convertNumCodeToCode((Double) jsCode);
        } else if (jsCode instanceof Float) {
            code = CoAPConstantsConverter.convertNumCodeToCode(((Float) jsCode).doubleValue());
        } else {
            throw new IllegalArgumentException( "JavaScriptCoapExchange.respond expects a String, Integer or Double as first argument but got "+jsCode);
        }
        return CoAP.ResponseCode.valueOf(code);
    }

    public void respond(String payload) {
        this.exchange.respond(payload);
    }

    public void respond(CoAP.ResponseCode code, String payload) {
        this.exchange.respond(code, payload);
    }

    public void respond(Object jsCode, String payload) {
        this.exchange.respond(getResponseCode(jsCode), payload);
    }

    public void respond(CoAP.ResponseCode code, byte[] payload) {
        this.exchange.respond(code, payload);
    }

    public void respond(Object jsCode, byte[] payload) {
        this.exchange.respond(getResponseCode(jsCode), payload);
    }

    public void respond(CoAP.ResponseCode code, byte[] payload, int contentFormat) {
        this.exchange.respond(code, payload, contentFormat);
    }

    public void respond(Object jsCode, byte[] payload, int contentFormat) {
        this.exchange.respond(getResponseCode(jsCode), payload, contentFormat);
    }

    public void respond(CoAP.ResponseCode code, String payload, int contentFormat) {
        this.exchange.respond(code, payload, contentFormat);
    }

    public void respond(Object jsCode, String payload, int contentFormat) {
        this.exchange.respond(getResponseCode(jsCode), payload, contentFormat);
    }

    public void respond(Response response) {
        this.exchange.respond(response);
    }

    public Exchange advanced() {
        return this.exchange.advanced();
    }
}

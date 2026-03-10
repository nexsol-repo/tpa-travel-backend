package com.nexsol.tpa.client.meritz.bridge.dto;

import java.util.Map;

public class MeritzBridgeResponse {

    private int status;

    private Map<String, Object> headers;

    private String body; // JSON string

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}

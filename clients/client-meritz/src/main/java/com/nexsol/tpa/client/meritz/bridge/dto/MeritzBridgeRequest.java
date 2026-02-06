package com.nexsol.tpa.client.meritz.bridge.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class MeritzBridgeRequest {

    private String insurerType;

    private String company;

    private String endpoint;

    private String method;

    private Map<String, String> headers;

    private Object body;

    public MeritzBridgeRequest(String company, String endpoint, String method, Map<String, String> headers,
            Object body) {
        this.insurerType = "MERITZ_TRAVEL";
        this.company = company;
        this.endpoint = endpoint;
        this.method = method;
        this.headers = headers;
        this.body = body;
    }

}

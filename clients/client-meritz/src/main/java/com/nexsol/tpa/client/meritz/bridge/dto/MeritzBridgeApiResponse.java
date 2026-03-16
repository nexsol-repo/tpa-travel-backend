package com.nexsol.tpa.client.meritz.bridge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeritzBridgeApiResponse {

    private boolean success;

    private String errCd;

    private String errMsg;

    private Object data;
}

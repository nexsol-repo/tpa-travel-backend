package com.nexsol.tpa.core.api.controller.v1.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthCertCompleteRequest {

    private Long contractId;

    private String impUid;

    private String requestId;

    private String moid;

    // tracking
    private String bizNum;

    private String pathRoot;

    private String pg;

    private String provider; // default DANAL_PASS
}

package com.nexsol.tpa.core.api.dto.v1;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthCertHistoryCompleteRequest {

    private String impUid; // 필수

    private String requestId; // optional

    private String moid; // optional (PortOne merchant_uid)

    private String pg; // optional

    private String provider; // optional (기본 DANAL_PASS)

    private String pathRoot; // optional (기본 dsf6)

    private String bizNum;

}

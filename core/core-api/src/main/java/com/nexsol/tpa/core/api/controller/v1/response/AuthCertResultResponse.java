package com.nexsol.tpa.core.api.controller.v1.response;

import com.nexsol.tpa.core.domain.auth.AuthCertResult;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthCertResultResponse {

    private String moid;

    private String impUid;

    private String requestId;

    private Long insuredSeqNo;

    private String uniqueKey;

    private String resultStatus;

    private String resultCode;

    private String resultMsg;

    private String certName;

    private String certBirthday;

    private String certGender;

    private String certPhone;

    private String matchedYn;

    private String matchFailReason;

    public static AuthCertResultResponse of(AuthCertResult r) {
        return AuthCertResultResponse.builder()
                .moid(r.moid())
                .impUid(r.impUid())
                .requestId(r.requestId())
                .insuredSeqNo(r.insuredSeqNo() != null ? r.insuredSeqNo().longValue() : null)
                .uniqueKey(r.uniqueKey())
                .resultStatus(r.resultStatus())
                .resultCode(r.resultCode())
                .resultMsg(r.resultMsg())
                .certName(r.certName())
                .certBirthday(r.certBirthday())
                .certGender(r.certGender())
                .certPhone(r.certPhone())
                .matchedYn(r.matchedYn())
                .matchFailReason(r.matchFailReason())
                .build();
    }
}

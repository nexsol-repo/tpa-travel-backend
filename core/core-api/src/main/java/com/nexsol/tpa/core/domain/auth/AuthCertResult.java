package com.nexsol.tpa.core.domain.auth;

public record AuthCertResult(
        String moid,
        String impUid,
        String requestId,
        Integer insuredSeqNo,
        String uniqueKey,
        String resultStatus,
        String resultCode,
        String resultMsg,
        String certName,
        String certBirthday,
        String certGender,
        String certPhone,
        String matchedYn,
        String matchFailReason) {}

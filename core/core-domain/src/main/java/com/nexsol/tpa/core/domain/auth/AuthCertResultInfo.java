package com.nexsol.tpa.core.domain.auth;

import lombok.Builder;

@Builder
public record AuthCertResultInfo(
        Long id,
        Long logId,
        String provider,
        String moid,
        String impUid,
        String requestId,
        String uniqueKey,
        String resultStatus,
        String resultCode,
        String resultMsg,
        String certName,
        String certBirthday,
        String certGender,
        String certPhone,
        boolean matched,
        String matchedYn,
        String matchFailReason,
        String rawResJson) {}

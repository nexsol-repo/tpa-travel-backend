package com.nexsol.tpa.core.domain.auth;

import lombok.Builder;

@Builder
public record AuthCertLogInfo(
        Long id,
        Long contractId,
        String bizNum,
        String impUid,
        String requestId,
        String pathRoot,
        String moid,
        String pg,
        String provider,
        String userAgent,
        String clientIp,
        String referer) {}

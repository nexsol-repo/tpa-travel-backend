package com.nexsol.tpa.core.domain.auth;

public record AuthCertHistory(
        String impUid,
        String requestId,
        String moid,
        String pg,
        String provider,
        String pathRoot,
        String bizNum) {}

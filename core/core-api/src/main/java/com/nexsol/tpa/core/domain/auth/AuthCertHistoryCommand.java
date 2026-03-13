package com.nexsol.tpa.core.domain.auth;

public record AuthCertHistoryCommand(
        String impUid,
        String requestId,
        String moid,
        String pg,
        String provider,
        String pathRoot,
        String bizNum) {}

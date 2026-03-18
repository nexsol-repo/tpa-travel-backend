package com.nexsol.tpa.core.domain.auth;

public record AuthCertCommand(
        Long contractId,
        String impUid,
        String requestId,
        String moid,
        String bizNum,
        String pathRoot,
        String pg,
        String provider) {}

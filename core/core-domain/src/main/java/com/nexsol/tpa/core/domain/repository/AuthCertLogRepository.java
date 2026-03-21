package com.nexsol.tpa.core.domain.repository;

import java.util.Optional;

import com.nexsol.tpa.core.domain.auth.AuthCertLogInfo;

public interface AuthCertLogRepository {
    AuthCertLogInfo save(AuthCertLogInfo logInfo);

    Optional<AuthCertLogInfo> findByImpUid(String impUid);

    Optional<AuthCertLogInfo> findByMoid(String moid);
}

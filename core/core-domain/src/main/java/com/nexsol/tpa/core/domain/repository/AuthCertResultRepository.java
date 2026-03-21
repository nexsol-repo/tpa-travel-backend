package com.nexsol.tpa.core.domain.repository;

import java.util.Optional;

import com.nexsol.tpa.core.domain.auth.AuthCertResultInfo;

public interface AuthCertResultRepository {
    AuthCertResultInfo save(AuthCertResultInfo resultInfo);

    Optional<AuthCertResultInfo> findByImpUid(String impUid);

    Optional<AuthCertResultInfo> findLatestByMoid(String moid);
}

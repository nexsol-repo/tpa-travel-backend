package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nexsol.tpa.core.domain.auth.AuthCertLogInfo;
import com.nexsol.tpa.core.domain.repository.AuthCertLogRepository;
import com.nexsol.tpa.storage.db.core.entity.TpaAuthCertLogEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaAuthCertLogRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultAuthCertLogRepository implements AuthCertLogRepository {

    private final JpaAuthCertLogRepository jpaRepository;

    @Override
    public AuthCertLogInfo save(AuthCertLogInfo logInfo) {
        TpaAuthCertLogEntity entity = TpaAuthCertLogEntity.fromDomain(logInfo);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<AuthCertLogInfo> findByImpUid(String impUid) {
        return jpaRepository.findByImpUid(impUid).map(TpaAuthCertLogEntity::toDomain);
    }

    @Override
    public Optional<AuthCertLogInfo> findByMoid(String moid) {
        return jpaRepository.findByMoid(moid).map(TpaAuthCertLogEntity::toDomain);
    }
}

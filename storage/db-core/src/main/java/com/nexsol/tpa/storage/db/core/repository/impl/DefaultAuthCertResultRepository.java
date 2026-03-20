package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.auth.AuthCertResultInfo;
import com.nexsol.tpa.core.domain.repository.AuthCertResultRepository;
import com.nexsol.tpa.storage.db.core.entity.TpaAuthCertResultEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaAuthCertResultRepository;

@Repository
@RequiredArgsConstructor
public class DefaultAuthCertResultRepository implements AuthCertResultRepository {

    private final JpaAuthCertResultRepository jpaRepository;

    @Override
    public AuthCertResultInfo save(AuthCertResultInfo resultInfo) {
        TpaAuthCertResultEntity entity = TpaAuthCertResultEntity.fromDomain(resultInfo);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<AuthCertResultInfo> findByImpUid(String impUid) {
        return jpaRepository.findByImpUid(impUid).map(TpaAuthCertResultEntity::toDomain);
    }

    @Override
    public Optional<AuthCertResultInfo> findLatestByMoid(String moid) {
        return jpaRepository.findTop1ByMoidOrderByCreatedAtDesc(moid).map(TpaAuthCertResultEntity::toDomain);
    }
}
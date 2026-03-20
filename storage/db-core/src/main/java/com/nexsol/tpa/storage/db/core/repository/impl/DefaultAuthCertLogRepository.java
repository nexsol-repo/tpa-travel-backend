package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.repository.AuthCertLogRepository;
import com.nexsol.tpa.storage.db.core.entity.TpaAuthCertLogEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaAuthCertLogRepository;

@Repository
@RequiredArgsConstructor
public class DefaultAuthCertLogRepository implements AuthCertLogRepository {

    private final JpaAuthCertLogRepository jpaRepository;

    @Override
    public TpaAuthCertLogEntity save(TpaAuthCertLogEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<TpaAuthCertLogEntity> findByImpUid(String impUid) {
        return jpaRepository.findByImpUid(impUid);
    }

    @Override
    public Optional<TpaAuthCertLogEntity> findByMoid(String moid) {
        return jpaRepository.findByMoid(moid);
    }
}
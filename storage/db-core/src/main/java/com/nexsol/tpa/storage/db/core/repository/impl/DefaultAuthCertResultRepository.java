package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.repository.AuthCertResultRepository;
import com.nexsol.tpa.storage.db.core.entity.TpaAuthCertResultEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaAuthCertResultRepository;

@Repository
@RequiredArgsConstructor
public class DefaultAuthCertResultRepository implements AuthCertResultRepository {

    private final JpaAuthCertResultRepository jpaRepository;

    @Override
    public TpaAuthCertResultEntity save(TpaAuthCertResultEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<TpaAuthCertResultEntity> findByImpUid(String impUid) {
        return jpaRepository.findByImpUid(impUid);
    }

    @Override
    public Optional<TpaAuthCertResultEntity> findLatestByMoid(String moid) {
        return jpaRepository.findTop1ByMoidOrderByCreatedAtDesc(moid);
    }
}
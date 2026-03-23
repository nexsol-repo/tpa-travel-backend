package com.nexsol.tpa.storage.db.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TpaAuthCertResultEntity;

public interface JpaAuthCertResultRepository extends JpaRepository<TpaAuthCertResultEntity, Long> {

    Optional<TpaAuthCertResultEntity> findByImpUid(String impUid);

    Optional<TpaAuthCertResultEntity> findTop1ByMoidOrderByCreatedAtDesc(String moid);
}

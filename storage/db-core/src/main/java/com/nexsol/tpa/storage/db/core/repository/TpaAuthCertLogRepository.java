package com.nexsol.tpa.storage.db.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TpaAuthCertLogEntity;

public interface TpaAuthCertLogRepository extends JpaRepository<TpaAuthCertLogEntity, Long> {

    Optional<TpaAuthCertLogEntity> findByImpUid(String impUid);

    Optional<TpaAuthCertLogEntity> findByMoid(String moid);
}

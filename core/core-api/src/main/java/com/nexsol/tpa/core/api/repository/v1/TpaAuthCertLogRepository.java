package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TpaAuthCertLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TpaAuthCertLogRepository extends JpaRepository<TpaAuthCertLogEntity, Long> {

    Optional<TpaAuthCertLogEntity> findByImpUid(String impUid);
    Optional<TpaAuthCertLogEntity> findByMoid(String moid);

}

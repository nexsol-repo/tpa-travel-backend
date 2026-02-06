package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TpaAuthCertResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TpaAuthCertResultRepository extends JpaRepository<TpaAuthCertResultEntity, Long> {

    Optional<TpaAuthCertResultEntity> findByImpUid(String impUid);
    Optional<TpaAuthCertResultEntity> findTop1ByMoidOrderByCreatedAtDesc(String moid);

}


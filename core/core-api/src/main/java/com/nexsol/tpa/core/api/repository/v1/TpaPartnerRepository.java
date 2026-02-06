package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TpaPartnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TpaPartnerRepository extends JpaRepository<TpaPartnerEntity, Long> {

    Optional<TpaPartnerEntity> findByPartnerCode(String partnerCode);

    List<TpaPartnerEntity> findAllByIsActiveTrueOrderByIdAsc();
}

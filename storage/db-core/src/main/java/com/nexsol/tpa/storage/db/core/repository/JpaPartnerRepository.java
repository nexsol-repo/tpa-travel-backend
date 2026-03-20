package com.nexsol.tpa.storage.db.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TpaPartnerEntity;

public interface JpaPartnerRepository extends JpaRepository<TpaPartnerEntity, Long> {

    Optional<TpaPartnerEntity> findByPartnerCode(String partnerCode);

    List<TpaPartnerEntity> findAllByIsActiveTrueOrderByIdAsc();
}

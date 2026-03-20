package com.nexsol.tpa.core.domain.repository;

import java.util.List;
import java.util.Optional;

import com.nexsol.tpa.core.domain.contract.Partner;

public interface PartnerRepository {
    Optional<Partner> findById(Long id);
    Optional<Partner> findByPartnerCode(String code);
    List<Partner> findAllActive();
}
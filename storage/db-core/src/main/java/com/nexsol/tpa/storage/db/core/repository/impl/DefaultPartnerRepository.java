package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.repository.PartnerRepository;
import com.nexsol.tpa.storage.db.core.entity.TpaPartnerEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaPartnerRepository;

@Repository
@RequiredArgsConstructor
public class DefaultPartnerRepository implements PartnerRepository {

    private final JpaPartnerRepository jpaRepository;

    @Override
    public Optional<TpaPartnerEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<TpaPartnerEntity> findByPartnerCode(String code) {
        return jpaRepository.findByPartnerCode(code);
    }

    @Override
    public List<TpaPartnerEntity> findAllActive() {
        return jpaRepository.findAllByIsActiveTrueOrderByIdAsc();
    }
}
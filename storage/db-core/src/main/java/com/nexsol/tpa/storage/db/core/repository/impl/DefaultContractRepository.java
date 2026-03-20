package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.repository.ContractRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaContractRepository;

@Repository
@RequiredArgsConstructor
public class DefaultContractRepository implements ContractRepository {

    private final JpaContractRepository jpaRepository;

    @Override
    public TravelContractEntity save(TravelContractEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<TravelContractEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<TravelContractEntity> findAllOrderByAuthUniqueKeyDesc(Pageable pageable) {
        return jpaRepository.findAllOrderByAuthUniqueKeyDesc(pageable);
    }

    @Override
    public Page<TravelContractEntity> findByAuthUniqueKey(String authUniqueKey, Pageable pageable) {
        return jpaRepository.findByAuthUniqueKey(authUniqueKey, pageable);
    }
}
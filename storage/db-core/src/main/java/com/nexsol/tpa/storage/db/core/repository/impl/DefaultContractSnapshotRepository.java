package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.nexsol.tpa.core.domain.repository.ContractSnapshotRepository;
import com.nexsol.tpa.storage.db.core.entity.TravelContractSnapshotEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaContractSnapshotRepository;

@Repository
@RequiredArgsConstructor
public class DefaultContractSnapshotRepository implements ContractSnapshotRepository {

    private final JpaContractSnapshotRepository jpaRepository;

    @Override
    public TravelContractSnapshotEntity save(TravelContractSnapshotEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<TravelContractSnapshotEntity> findByContractIdAndSnapshotType(
            Long contractId, String snapshotType) {
        return jpaRepository.findByContractIdAndSnapshotType(contractId, snapshotType);
    }
}
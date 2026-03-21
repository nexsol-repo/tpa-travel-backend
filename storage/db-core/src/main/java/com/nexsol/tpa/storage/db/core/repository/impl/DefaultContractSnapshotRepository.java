package com.nexsol.tpa.storage.db.core.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nexsol.tpa.core.domain.repository.ContractSnapshotRepository;
import com.nexsol.tpa.core.domain.snapshot.ContractSnapshot;
import com.nexsol.tpa.storage.db.core.entity.TravelContractSnapshotEntity;
import com.nexsol.tpa.storage.db.core.repository.JpaContractSnapshotRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DefaultContractSnapshotRepository implements ContractSnapshotRepository {

    private final JpaContractSnapshotRepository jpaRepository;

    @Override
    public ContractSnapshot save(ContractSnapshot snapshot) {
        TravelContractSnapshotEntity entity = TravelContractSnapshotEntity.fromDomain(snapshot);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<ContractSnapshot> findByContractIdAndSnapshotType(
            Long contractId, String snapshotType) {
        return jpaRepository
                .findByContractIdAndSnapshotType(contractId, snapshotType)
                .map(TravelContractSnapshotEntity::toDomain);
    }
}

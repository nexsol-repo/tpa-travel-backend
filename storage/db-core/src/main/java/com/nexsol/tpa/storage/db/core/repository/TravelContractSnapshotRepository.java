package com.nexsol.tpa.storage.db.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexsol.tpa.storage.db.core.entity.TravelContractSnapshotEntity;

public interface TravelContractSnapshotRepository
        extends JpaRepository<TravelContractSnapshotEntity, Long> {

    Optional<TravelContractSnapshotEntity> findByContractIdAndSnapshotType(
            Long contractId, String snapshotType);
}

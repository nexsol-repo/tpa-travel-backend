package com.nexsol.tpa.core.domain.repository;

import java.util.Optional;

import com.nexsol.tpa.core.domain.snapshot.ContractSnapshot;

public interface ContractSnapshotRepository {
    ContractSnapshot save(ContractSnapshot snapshot);
    Optional<ContractSnapshot> findByContractIdAndSnapshotType(Long contractId, String snapshotType);
}
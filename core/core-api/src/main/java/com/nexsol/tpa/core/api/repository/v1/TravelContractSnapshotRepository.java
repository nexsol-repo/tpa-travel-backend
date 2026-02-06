package com.nexsol.tpa.core.api.repository.v1;

import com.nexsol.tpa.core.api.entity.TravelContractSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelContractSnapshotRepository extends JpaRepository<TravelContractSnapshotEntity, Long> {

    Optional<TravelContractSnapshotEntity> findByContractIdAndSnapshotType(Long contractId, String snapshotType);
}

package com.nexsol.tpa.core.domain.snapshot;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.storage.db.core.entity.TravelContractSnapshotEntity;
import com.nexsol.tpa.storage.db.core.repository.TravelContractSnapshotRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SnapshotAppender {

    private final TravelContractSnapshotRepository snapshotRepository;

    public void append(Long contractId, Long insurerId, String snapshotType, String jsonSnapshot) {
        snapshotRepository.save(
                TravelContractSnapshotEntity.builder()
                        .contractId(contractId)
                        .insurerId(insurerId)
                        .method("api")
                        .snapshotType(snapshotType)
                        .jsonSnapshot(jsonSnapshot)
                        .build());
    }
}

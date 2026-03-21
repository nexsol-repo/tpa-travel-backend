package com.nexsol.tpa.core.domain.snapshot;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.ContractSnapshotRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SnapshotAppender {

    private final ContractSnapshotRepository snapshotRepository;

    public void append(Long contractId, Long insurerId, String snapshotType, String jsonSnapshot) {
        snapshotRepository.save(
                ContractSnapshot.builder()
                        .contractId(contractId)
                        .insurerId(insurerId)
                        .method("api")
                        .snapshotType(snapshotType)
                        .jsonSnapshot(jsonSnapshot)
                        .build());
    }
}

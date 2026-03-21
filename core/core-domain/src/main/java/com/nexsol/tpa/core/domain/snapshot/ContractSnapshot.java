package com.nexsol.tpa.core.domain.snapshot;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ContractSnapshot(
        Long id,
        Long contractId,
        Long insurerId,
        String method,
        String snapshotType,
        String jsonSnapshot,
        LocalDateTime createdAt) {}

package com.nexsol.tpa.storage.db.core.entity;

import java.time.LocalDateTime;

import com.nexsol.tpa.core.domain.snapshot.ContractSnapshot;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_contract_snapshot")
public class TravelContractSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // apply 시점에 매핑
    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "insurer_id", nullable = false)
    private Long insurerId;

    @Column(name = "method", nullable = false, length = 10)
    private String method; // api 등

    @Column(name = "snapshot_type", nullable = false, length = 30)
    private String snapshotType; // QUOTE / PAYMENT / CANCEL ...

    @Lob
    @Column(name = "json_snapshot", nullable = false, columnDefinition = "longtext")
    private String jsonSnapshot;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TravelContractSnapshotEntity(
            Long contractId,
            Long insurerId,
            String method,
            String snapshotType,
            String jsonSnapshot) {
        this.contractId = contractId;
        this.insurerId = insurerId;
        this.method = method;
        this.snapshotType = snapshotType;
        this.jsonSnapshot = jsonSnapshot;
    }

    public void updateSnapshot(String jsonSnapshot) {
        this.jsonSnapshot = jsonSnapshot;
    }

    public ContractSnapshot toDomain() {
        return ContractSnapshot.builder()
                .id(id)
                .contractId(contractId)
                .insurerId(insurerId)
                .method(method)
                .snapshotType(snapshotType)
                .jsonSnapshot(jsonSnapshot)
                .createdAt(createdAt)
                .build();
    }

    public static TravelContractSnapshotEntity fromDomain(ContractSnapshot s) {
        TravelContractSnapshotEntity entity =
                TravelContractSnapshotEntity.builder()
                        .contractId(s.contractId())
                        .insurerId(s.insurerId())
                        .method(s.method())
                        .snapshotType(s.snapshotType())
                        .jsonSnapshot(s.jsonSnapshot())
                        .build();
        return entity;
    }
}

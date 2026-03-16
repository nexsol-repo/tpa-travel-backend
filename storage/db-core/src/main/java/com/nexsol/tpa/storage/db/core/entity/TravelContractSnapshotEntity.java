package com.nexsol.tpa.storage.db.core.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
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

    public void updateSnapshot(String jsonSnapshot) {
        this.jsonSnapshot = jsonSnapshot;
    }
}

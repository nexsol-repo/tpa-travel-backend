package com.nexsol.tpa.core.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "travel_insurance_coverage")
public class TravelInsuranceCoverageEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "insurer_id", nullable = false)
    private Long insurerId;

    @Column(name = "group_code")
    private String groupCode;

    @Column(name = "coverage_code", length = 50)
    private String coverageCode;

    @Column(name = "coverage_name", nullable = false, length = 100)
    private String coverageName;

    @Column(name = "claim_reason", length = 300)
    private String claimReason;

    @Lob
    @Column(name = "claim_content", columnDefinition = "text")
    private String claimContent;

    @Column(name = "sub_title", length = 200)
    private String subTitle;

    @Lob
    @Column(name = "sub_content", columnDefinition = "text")
    private String subContent;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

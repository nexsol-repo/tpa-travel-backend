package com.nexsol.tpa.storage.db.core.entity;

import com.nexsol.tpa.core.domain.coverage.Coverage;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_coverage")
public class TravelCoverageEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "insurer_id", nullable = false)
    private Long insurerId;

    @Column(name = "coverage_code", nullable = false, length = 50)
    private String coverageCode;

    @Column(name = "coverage_name", nullable = false, length = 200)
    private String coverageName;

    @Column(name = "section_code", nullable = false, length = 30)
    private String sectionCode;

    @Column(name = "claim_reason", length = 500)
    private String claimReason;

    @Lob
    @Column(name = "claim_content", columnDefinition = "text")
    private String claimContent;

    @Column(name = "sub_title", length = 200)
    private String subTitle;

    @Lob
    @Column(name = "sub_content", columnDefinition = "text")
    private String subContent;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public Coverage toDomain() {
        return Coverage.builder()
                .id(id)
                .insurerId(insurerId)
                .coverageCode(coverageCode)
                .coverageName(coverageName)
                .sectionCode(sectionCode)
                .claimReason(claimReason)
                .claimContent(claimContent)
                .subTitle(subTitle)
                .subContent(subContent)
                .build();
    }
}

package com.nexsol.tpa.storage.db.core.entity;

import com.nexsol.tpa.core.domain.coverage.FamilyCoverage;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "travel_family_coverage",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_family_coverage",
                    columnNames = {"family_id", "coverage_id"})
        })
public class TravelFamilyCoverageEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "coverage_id", nullable = false)
    private Long coverageId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured;

    @Column(name = "display_name", length = 200)
    private String displayName;

    public FamilyCoverage toDomain() {
        return FamilyCoverage.builder()
                .id(id)
                .familyId(familyId)
                .coverageId(coverageId)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .featured(Boolean.TRUE.equals(isFeatured))
                .displayName(displayName)
                .build();
    }
}

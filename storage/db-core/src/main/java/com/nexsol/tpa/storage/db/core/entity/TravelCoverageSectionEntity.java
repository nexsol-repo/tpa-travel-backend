package com.nexsol.tpa.storage.db.core.entity;

import com.nexsol.tpa.core.domain.coverage.CoverageSection;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_coverage_section")
public class TravelCoverageSectionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_code", nullable = false, length = 30)
    private String sectionCode;

    @Column(name = "section_name", nullable = false, length = 100)
    private String sectionName;

    @Column(name = "description")
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public CoverageSection toDomain() {
        return CoverageSection.builder()
                .id(id)
                .sectionCode(sectionCode)
                .sectionName(sectionName)
                .description(description)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .active(Boolean.TRUE.equals(isActive))
                .build();
    }
}

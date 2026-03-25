package com.nexsol.tpa.core.domain.coverage;

import lombok.Builder;

@Builder
public record FamilyCoverageDetail(
        Long familyId,
        Coverage coverage,
        int sortOrder,
        boolean featured,
        String displayName,
        String sectionName) {

    public static FamilyCoverageDetail of(
            FamilyCoverage fc, Coverage coverage, String sectionName) {
        return FamilyCoverageDetail.builder()
                .familyId(fc.familyId())
                .coverage(coverage)
                .sortOrder(fc.sortOrder())
                .featured(fc.featured())
                .displayName(fc.displayName())
                .sectionName(sectionName)
                .build();
    }

    public String resolvedName() {
        return (displayName != null && !displayName.isBlank())
                ? displayName
                : coverage.coverageName();
    }

    public String sectionCode() {
        return coverage.sectionCode();
    }

    public String coverageCode() {
        return coverage.coverageCode();
    }
}

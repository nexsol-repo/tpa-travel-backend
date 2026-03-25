package com.nexsol.tpa.core.domain.coverage;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.plan.PlanFamily;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoverageService {

    private final FamilyCoverageReader familyCoverageReader;
    private final CoverageSectionReader coverageSectionReader;

    public Map<Long, List<FamilyCoverageDetail>> findCoveragesForFamilies(
            List<PlanFamily> families) {
        List<Long> familyIds = families.stream().map(PlanFamily::familyId).toList();
        return familyCoverageReader.readByFamilyIds(familyIds);
    }

    public List<FamilyCoverageDetail> findCoverages(Long familyId) {
        return familyCoverageReader.readByFamilyId(familyId);
    }

    public CoverageResult getCoverage(Long insurerId, String coverageCode) {
        Coverage coverage = familyCoverageReader.readCoverage(insurerId, coverageCode);

        CoverageSection section =
                coverageSectionReader.readAllActiveAsMap().get(coverage.sectionCode());

        return CoverageResult.builder()
                .id(coverage.id())
                .coverageCode(coverage.coverageCode())
                .coverageName(coverage.coverageName())
                .sectionCode(coverage.sectionCode())
                .sectionName(section != null ? section.sectionName() : null)
                .claimReason(coverage.claimReason())
                .claimContent(coverage.claimContent())
                .subTitle(coverage.subTitle())
                .subContent(coverage.subContent())
                .build();
    }
}

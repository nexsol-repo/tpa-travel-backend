package com.nexsol.tpa.core.domain.coverage;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.CoverageRepository;
import com.nexsol.tpa.core.domain.repository.FamilyCoverageRepository;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FamilyCoverageReader {

    private final FamilyCoverageRepository familyCoverageRepository;
    private final CoverageRepository coverageRepository;
    private final CoverageSectionReader coverageSectionReader;

    public List<FamilyCoverageDetail> readByFamilyId(Long familyId) {
        List<FamilyCoverage> familyCoverages = familyCoverageRepository.findByFamilyId(familyId);
        if (familyCoverages.isEmpty()) {
            return List.of();
        }
        return toDetails(familyCoverages);
    }

    public Map<Long, List<FamilyCoverageDetail>> readByFamilyIds(List<Long> familyIds) {
        List<FamilyCoverage> familyCoverages = familyCoverageRepository.findByFamilyIds(familyIds);
        if (familyCoverages.isEmpty()) {
            return Map.of();
        }
        List<FamilyCoverageDetail> details = toDetails(familyCoverages);
        return details.stream().collect(Collectors.groupingBy(FamilyCoverageDetail::familyId));
    }

    public Coverage readCoverage(Long insurerId, String coverageCode) {
        return coverageRepository
                .findByInsurerIdAndCoverageCode(insurerId, coverageCode)
                .orElseThrow(
                        () ->
                                new CoreException(
                                        CoreErrorType.NOT_FOUND_DATA,
                                        "coverage not found: " + coverageCode));
    }

    private List<FamilyCoverageDetail> toDetails(List<FamilyCoverage> familyCoverages) {
        List<Long> coverageIds =
                familyCoverages.stream().map(FamilyCoverage::coverageId).distinct().toList();

        Map<Long, Coverage> coverageMap =
                coverageRepository.findAllByIds(coverageIds).stream()
                        .collect(Collectors.toMap(Coverage::id, Function.identity()));

        Map<String, CoverageSection> sectionMap = coverageSectionReader.readAllActiveAsMap();

        return familyCoverages.stream()
                .map(
                        fc -> {
                            Coverage coverage = coverageMap.get(fc.coverageId());
                            String sectionName = null;
                            int sectionSortOrder = Integer.MAX_VALUE;
                            if (coverage != null) {
                                CoverageSection section = sectionMap.get(coverage.sectionCode());
                                if (section != null) {
                                    sectionName = section.sectionName();
                                    sectionSortOrder = section.sortOrder();
                                }
                            }
                            return FamilyCoverageDetail.of(
                                    fc, coverage, sectionName, sectionSortOrder);
                        })
                .filter(detail -> detail.coverage() != null)
                .sorted(
                        java.util.Comparator.comparingInt(FamilyCoverageDetail::sectionSortOrder)
                                .thenComparingInt(FamilyCoverageDetail::sortOrder))
                .toList();
    }
}

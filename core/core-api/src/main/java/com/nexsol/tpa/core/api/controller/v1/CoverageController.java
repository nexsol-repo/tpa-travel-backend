package com.nexsol.tpa.core.api.controller.v1;

import com.nexsol.tpa.core.api.dto.v1.CoverageResponse;
import com.nexsol.tpa.core.api.entity.TravelInsuranceCoverageEntity;
import com.nexsol.tpa.core.api.repository.v1.TravelInsuranceCoverageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/meritz/travel/coverages")
public class CoverageController {

    private final TravelInsuranceCoverageRepository repository;

    /**
     * 담보 단건 조회
     * ex) GET /v1/coverages?coverageCode=151910&insurerId=1
     */
    @GetMapping
    public CoverageResponse getCoverage(
            @RequestParam(name = "insurerId") Long insurerId,
            @RequestParam(name = "coverageCode") String coverageCode
    ) {

        TravelInsuranceCoverageEntity entity =
                repository.findByInsurerIdAndCoverageCodeAndDeletedAtIsNull(insurerId, coverageCode)
                        .orElseThrow(() ->
                                new IllegalArgumentException("coverage not found: " + coverageCode));

        return CoverageResponse.builder()
                .id(entity.getId())
                .coverageCode(entity.getCoverageCode())
                .coverageName(entity.getCoverageName())
                .groupCode(entity.getGroupCode())
                .claimReason(entity.getClaimReason())
                .claimContent(entity.getClaimContent())
                .subTitle(entity.getSubTitle())
                .subContent(entity.getSubContent())
                .build();
    }
}
package com.nexsol.tpa.core.api.controller.v1;

import org.springframework.web.bind.annotation.*;

import com.nexsol.tpa.core.api.controller.v1.response.CoverageResponse;
import com.nexsol.tpa.core.domain.coverage.CoverageResult;
import com.nexsol.tpa.core.domain.coverage.CoverageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/meritz/travel/coverages")
public class CoverageController {

    private final CoverageService coverageService;

    /**
     * 담보 단건 조회 ex) GET /v1/coverages?coverageCode=151910&insurerId=1
     */
    @GetMapping
    public CoverageResponse getCoverage(
            @RequestParam(name = "insurerId") Long insurerId,
            @RequestParam(name = "coverageCode") String coverageCode) {
        CoverageResult coverage = coverageService.getCoverage(insurerId, coverageCode);

        return CoverageResponse.builder()
                .id(coverage.id())
                .coverageCode(coverage.coverageCode())
                .coverageName(coverage.coverageName())
                .groupCode(coverage.groupCode())
                .claimReason(coverage.claimReason())
                .claimContent(coverage.claimContent())
                .subTitle(coverage.subTitle())
                .subContent(coverage.subContent())
                .build();
    }
}

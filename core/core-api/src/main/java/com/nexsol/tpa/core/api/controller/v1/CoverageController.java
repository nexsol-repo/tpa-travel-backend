package com.nexsol.tpa.core.api.controller.v1;

import org.springframework.web.bind.annotation.*;

import com.nexsol.tpa.core.api.controller.v1.response.CoverageResponse;
import com.nexsol.tpa.core.domain.coverage.CoverageResult;
import com.nexsol.tpa.core.domain.coverage.CoverageService;
import com.nexsol.tpa.core.support.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/travel/coverages")
public class CoverageController {

    private final CoverageService coverageService;

    @GetMapping
    public ApiResponse<CoverageResponse> getCoverage(
            @RequestParam(name = "insurerId") Long insurerId,
            @RequestParam(name = "coverageCode") String coverageCode) {
        CoverageResult coverage = coverageService.getCoverage(insurerId, coverageCode);
        return ApiResponse.success(CoverageResponse.of(coverage));
    }
}

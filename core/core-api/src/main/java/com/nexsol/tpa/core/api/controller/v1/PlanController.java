package com.nexsol.tpa.core.api.controller.v1;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.nexsol.tpa.core.api.controller.v1.request.PlanRequest;
import com.nexsol.tpa.core.api.controller.v1.response.PlanCoverageResponse;
import com.nexsol.tpa.core.api.controller.v1.response.PlanListResponse;
import com.nexsol.tpa.core.domain.coverage.CoverageService;
import com.nexsol.tpa.core.domain.coverage.FamilyCoverageDetail;
import com.nexsol.tpa.core.domain.plan.PlanFamily;
import com.nexsol.tpa.core.domain.plan.PlanService;
import com.nexsol.tpa.core.domain.premium.PlanCondition;
import com.nexsol.tpa.core.domain.premium.Premium;
import com.nexsol.tpa.core.domain.premium.PremiumService;
import com.nexsol.tpa.core.support.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/travel")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;
    private final CoverageService coverageService;
    private final PremiumService premiumService;

    @PostMapping(value = "/plans", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PlanListResponse> plans(@Valid @RequestBody PlanRequest request) {
        PlanCondition cmd = request.toPlanCondition();
        List<PlanFamily> families = planService.findQuoteFamilies(cmd);
        Map<Long, Premium> premiums = premiumService.calculateAll(cmd, families);
        Map<Long, List<FamilyCoverageDetail>> coverages =
                coverageService.findCoveragesForFamilies(families);
        Map<Long, Long> silsonExcludeMap = planService.resolveSilsonExcludeMap(cmd);

        return ApiResponse.success(
                PlanListResponse.of(cmd, families, premiums, coverages, silsonExcludeMap));
    }

    @PostMapping(value = "/plans/{planId}/coverages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<PlanCoverageResponse> planCoverages(
            @PathVariable Long planId, @Valid @RequestBody PlanRequest request) {
        PlanFamily family = planService.findFamilyByPlanId(request.toPlanCondition(), planId);
        Premium premium =
                premiumService.calculateSingle(
                        request.toPlanCondition(), family, request.getRepresentativeIndex());
        List<FamilyCoverageDetail> coverages = coverageService.findCoverages(family.familyId());

        return ApiResponse.success(PlanCoverageResponse.of(family, premium, coverages));
    }
}



package com.nexsol.tpa.core.api.controller.v1;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.nexsol.tpa.core.api.controller.v1.request.PlanRequest;
import com.nexsol.tpa.core.api.controller.v1.response.PlanCoverageResponse;
import com.nexsol.tpa.core.api.controller.v1.response.PlanListResponse;
import com.nexsol.tpa.core.domain.coverage.TravelCoverageService;
import com.nexsol.tpa.core.domain.plan.TravelPlanReader.PlanFamily;
import com.nexsol.tpa.core.domain.plan.TravelPlanService;
import com.nexsol.tpa.core.domain.premium.PlanCondition;
import com.nexsol.tpa.core.domain.premium.PremiumResult;
import com.nexsol.tpa.core.domain.premium.PremiumService;
import com.nexsol.tpa.core.domain.premium.QuoteResult;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/meritz/travel")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService planService;
    private final TravelCoverageService coverageService;
    private final PremiumService premiumService;

    @PostMapping(value = "/plans", produces = MediaType.APPLICATION_JSON_VALUE)
    public PlanListResponse plans(@Valid @RequestBody PlanRequest request) {
        PlanCondition cmd = request.toPlanCondition();
        List<PlanFamily> families = planService.findQuoteFamilies(cmd);
        Map<Long, PremiumResult> premiums = premiumService.calculateAll(cmd, families);
        Map<Long, List<QuoteResult.DbCoverage>> coverages =
                coverageService.findCoveragesForFamilies(families);

        boolean silsonExclude = cmd.silsonExclude() != null && cmd.silsonExclude();
        Map<Long, Long> silsonExcludeMap =
                silsonExclude
                        ? Collections.emptyMap()
                        : planService.findSilsonExcludePlanIdMap(cmd);

        return PlanListResponse.of(cmd, families, premiums, coverages, silsonExcludeMap);
    }

    @PostMapping(value = "/plans/{planId}/coverages", produces = MediaType.APPLICATION_JSON_VALUE)
    public PlanCoverageResponse planCoverages(
            @PathVariable Long planId, @Valid @RequestBody PlanRequest request) {
        PlanFamily family = planService.findFamilyByPlanId(request.toPlanCondition(), planId);
        PremiumResult premium =
                premiumService.calculateSingle(
                        request.toPlanCondition(), family, request.getRepresentativeIndex());
        List<QuoteResult.DbCoverage> coverages =
                coverageService.findCoverages(family.repPlan().getId());
        return PlanCoverageResponse.of(family, premium, coverages);
    }
}

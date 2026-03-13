package com.nexsol.tpa.core.api.controller.v1;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.nexsol.tpa.core.api.controller.v1.request.PlanRequest;
import com.nexsol.tpa.core.api.controller.v1.response.PlanDetailResponse;
import com.nexsol.tpa.core.api.controller.v1.response.PlanListResponse;
import com.nexsol.tpa.core.api.controller.v1.response.QuoteResponse;
import com.nexsol.tpa.core.domain.coverage.TravelCoverageService;
import com.nexsol.tpa.core.domain.plan.TravelPlanReader.PlanFamily;
import com.nexsol.tpa.core.domain.plan.TravelPlanService;
import com.nexsol.tpa.core.domain.premium.PremiumResult;
import com.nexsol.tpa.core.domain.premium.PremiumService;
import com.nexsol.tpa.core.domain.premium.QuoteResult;
import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;

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
        List<PlanFamily> families = planService.findQuoteFamilies(request.toPlanCondition());
        Map<Long, PremiumResult> premiums =
                premiumService.calculateAll(request.toPlanCondition(), families);
        Map<Long, List<QuoteResult.DbCoverage>> coverages =
                coverageService.findCoveragesForFamilies(families);
        return PlanListResponse.of(request.toPlanCondition(), families, premiums, coverages);
    }

    @PostMapping(value = "/quote", produces = MediaType.APPLICATION_JSON_VALUE)
    public QuoteResponse quote(@Valid @RequestBody PlanRequest request) {
        List<PlanFamily> families = planService.findQuoteFamilies(request.toPlanCondition());
        Map<Long, PremiumResult> premiums =
                premiumService.calculateAll(request.toPlanCondition(), families);
        Map<Long, List<QuoteResult.DbCoverage>> coverages =
                coverageService.findCoveragesForFamilies(families);
        return QuoteResponse.of(request.toPlanCondition(), families, premiums, coverages);
    }

    @PostMapping(value = "/plans/{planId}/premium", produces = MediaType.APPLICATION_JSON_VALUE)
    public PlanDetailResponse planPremium(
            @PathVariable Long planId, @Valid @RequestBody PlanRequest request) {
        PlanFamily family = planService.findFamilyByPlanId(request.toPlanCondition(), planId);
        PremiumResult premium =
                premiumService.calculateSingle(
                        request.toPlanCondition(), family, request.getRepresentativeIndex());
        List<QuoteResult.DbCoverage> coverages =
                coverageService.findCoverages(family.repPlan().getId());
        return PlanDetailResponse.of(request.toPlanCondition(), family, premium, coverages);
    }

    @PostMapping(value = "/plans/{planId}/coverages", produces = MediaType.APPLICATION_JSON_VALUE)
    public QuoteResponse.PlanCard planCoverages(
            @PathVariable Long planId, @Valid @RequestBody PlanRequest request) {
        QuoteResponse quoteResponse = quote(request);
        return quoteResponse.getPlans().stream()
                .filter(p -> Objects.equals(p.getPlanId(), planId))
                .findFirst()
                .orElseThrow(
                        () ->
                                new CoreApiException(
                                        CoreApiErrorType.QUOTE_PLAN_NOT_FOUND, "planId=" + planId));
    }

    @PostMapping(
            value = "/plans/{planId}/silson-exclude",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public QuoteResponse.PlanCard silsonExclude(
            @PathVariable Long planId, @Valid @RequestBody PlanRequest request) {
        PlanFamily silsonFamily =
                planService.findSilsonExcludeFamily(request.toPlanCondition(), planId);
        PremiumResult premium =
                premiumService.calculateSingle(
                        request.toPlanCondition(), silsonFamily, request.getRepresentativeIndex());
        List<QuoteResult.DbCoverage> coverages =
                coverageService.findCoverages(silsonFamily.repPlan().getId());
        return QuoteResponse.toPlanCard(silsonFamily, premium, coverages);
    }
}
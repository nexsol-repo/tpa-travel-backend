package com.nexsol.tpa.core.api.controller.v1.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanSummaryResponse {

    private Long familyId;

    private Long planId;

    private String planGrpCd;

    private String planCd;

    private String planNm;

    private String planNmRaw;

    private Long silsonExcludePlanId;

    private long totalPremium;

    private String currency;

    private List<RepresentativeCoverageResponse> representativeCoverages;
}

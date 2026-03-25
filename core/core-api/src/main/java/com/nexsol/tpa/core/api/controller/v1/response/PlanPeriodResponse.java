package com.nexsol.tpa.core.api.controller.v1.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanPeriodResponse {

    private String insBgnDt;

    private String insEdDt;
}

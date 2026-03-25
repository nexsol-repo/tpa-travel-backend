package com.nexsol.tpa.core.api.controller.v1.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PremiumSummary {

    private long ttPrem;

    private String currency;
}

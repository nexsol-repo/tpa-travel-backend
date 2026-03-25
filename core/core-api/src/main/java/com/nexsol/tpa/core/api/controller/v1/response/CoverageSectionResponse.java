package com.nexsol.tpa.core.api.controller.v1.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CoverageSectionResponse {

    private String sectionCode;

    private String sectionName;

    private List<CoverageAmountResponse> coverages;
}

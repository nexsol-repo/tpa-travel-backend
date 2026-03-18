package com.nexsol.tpa.core.api.controller.v1.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CoverageResponse {

    private Long id;

    private String coverageCode;

    private String coverageName;

    private String groupCode;

    private String claimReason;

    private String claimContent;

    private String subTitle;

    private String subContent;
}

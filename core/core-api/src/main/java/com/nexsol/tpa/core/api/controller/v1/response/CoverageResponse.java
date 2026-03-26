package com.nexsol.tpa.core.api.controller.v1.response;

import com.nexsol.tpa.core.domain.coverage.CoverageResult;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CoverageResponse {

    private Long id;

    private String coverageCode;

    private String coverageName;

    private String sectionCode;

    private String sectionName;

    private String claimReason;

    private String claimContent;

    private String subTitle;

    private String subContent;

    public static CoverageResponse of(CoverageResult coverage) {
        return CoverageResponse.builder()
                .id(coverage.id())
                .coverageCode(coverage.coverageCode())
                .coverageName(coverage.coverageName())
                .sectionCode(coverage.sectionCode())
                .sectionName(coverage.sectionName())
                .claimReason(coverage.claimReason())
                .claimContent(coverage.claimContent())
                .subTitle(coverage.subTitle())
                .subContent(coverage.subContent())
                .build();
    }
}

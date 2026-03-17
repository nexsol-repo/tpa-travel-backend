package com.nexsol.tpa.core.api.controller.v1.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContractListRequest {

    private String polNo;

    private String quotReqNo;

    private String ctrStDt;

    private String ctrEdDt;
}
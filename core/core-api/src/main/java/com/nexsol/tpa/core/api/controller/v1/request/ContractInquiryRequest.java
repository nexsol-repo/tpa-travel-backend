package com.nexsol.tpa.core.api.controller.v1.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContractInquiryRequest {

    private String polNo;

    private String ctrNo;
}
package com.nexsol.tpa.core.api.controller.v1.response;

import com.nexsol.tpa.core.domain.apply.ApplyResult;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContractApplyResponse {

    private Long contractId;

    private String status;

    public static ContractApplyResponse of(ApplyResult result) {
        return ContractApplyResponse.builder()
                .contractId(result.contractId())
                .status(result.status())
                .build();
    }
}

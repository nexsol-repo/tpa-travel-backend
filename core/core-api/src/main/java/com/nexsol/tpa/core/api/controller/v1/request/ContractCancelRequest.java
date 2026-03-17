package com.nexsol.tpa.core.api.controller.v1.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ContractCancelRequest {

    private final Long contractId;

    @Builder
    private ContractCancelRequest(Long contractId) {
        this.contractId = contractId;
    }
}

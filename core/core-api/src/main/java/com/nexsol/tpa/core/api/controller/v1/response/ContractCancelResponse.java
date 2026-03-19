package com.nexsol.tpa.core.api.controller.v1.response;

public record ContractCancelResponse(Long contractId) {

    public static ContractCancelResponse of(Long contractId) {
        return new ContractCancelResponse(contractId);
    }
}
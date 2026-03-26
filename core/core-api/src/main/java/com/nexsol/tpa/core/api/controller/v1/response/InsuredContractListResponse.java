package com.nexsol.tpa.core.api.controller.v1.response;

import java.util.List;

import com.nexsol.tpa.core.domain.inquiry.InsuredContractSummary;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InsuredContractListResponse {

    private List<InsuredContractSummary> contracts;

    public static InsuredContractListResponse of(List<InsuredContractSummary> contracts) {
        return InsuredContractListResponse.builder().contracts(contracts).build();
    }
}

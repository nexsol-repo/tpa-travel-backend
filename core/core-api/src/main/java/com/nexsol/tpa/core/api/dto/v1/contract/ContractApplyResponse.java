package com.nexsol.tpa.core.api.dto.v1.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContractApplyResponse {

    private Long contractId;

    private String status; // PENDING

}

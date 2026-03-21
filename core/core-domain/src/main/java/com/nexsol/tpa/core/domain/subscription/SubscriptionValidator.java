package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.contract.ContractValidator;

@Component
public class SubscriptionValidator {

    public void validate(SubscriptionCommand cmd, ContractInfo contract) {
        ContractValidator.requireNotBlank(cmd.cardNo(), "cardNo is required");
        ContractValidator.requireNotBlank(cmd.efctPrd(), "efctPrd is required");
        ContractValidator.requireNotBlank(cmd.dporNm(), "dporNm is required");
        ContractValidator.requireNotBlank(cmd.dporCd(), "dporCd is required");
        ContractValidator.requireNotBlank(
                contract.policyNumber(), "policyNumber(polNo) is required");
    }
}

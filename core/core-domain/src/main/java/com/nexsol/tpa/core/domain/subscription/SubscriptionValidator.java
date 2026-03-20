package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.contract.ContractValidator;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;

@Component
public class SubscriptionValidator {

    public void validate(SubscriptionCommand cmd, TravelContractEntity contract) {
        ContractValidator.requireNotBlank(cmd.cardNo(), "cardNo is required");
        ContractValidator.requireNotBlank(cmd.efctPrd(), "efctPrd is required");
        ContractValidator.requireNotBlank(cmd.dporNm(), "dporNm is required");
        ContractValidator.requireNotBlank(cmd.dporCd(), "dporCd is required");
        ContractValidator.requireNotBlank(
                contract.getPolicyNumber(), "policyNumber(polNo) is required");
    }
}

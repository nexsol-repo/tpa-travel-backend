package com.nexsol.tpa.core.domain.subscription;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.contract.AuthInfo;
import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.contract.ContractValidator;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

@Component
public class SubscriptionValidator {

    public void validate(SubscriptionCommand cmd, ContractInfo contract) {
        ContractValidator.requireNotBlank(cmd.cardNo(), "cardNo is required");
        ContractValidator.requireNotBlank(cmd.efctPrd(), "efctPrd is required");
        ContractValidator.requireNotBlank(cmd.dporNm(), "dporNm is required");
        ContractValidator.requireNotBlank(cmd.dporCd(), "dporCd is required");
        ContractValidator.requireNotBlank(
                contract.policyNumber(), "policyNumber(polNo) is required");

        switch (contract.auth()) {
            case AuthInfo(var provider, var impUid, _, var uniqueKey, var status, _)
                    when provider != null
                            && !provider.isBlank()
                            && impUid != null
                            && !impUid.isBlank()
                            && uniqueKey != null
                            && !uniqueKey.isBlank()
                            && "SUCCESS".equals(status) -> {}
            case null, default -> throw new CoreException(CoreErrorType.AUTH_NOT_COMPLETED);
        }
    }
}

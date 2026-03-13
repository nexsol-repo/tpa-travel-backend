package com.nexsol.tpa.core.domain.contract;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.enums.TravelContractStatus;
import com.nexsol.tpa.core.enums.TravelPaymentStatus;
import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurePaymentEntity;

@Component
public class ContractValidator {

    public void requirePending(TravelContractEntity contract) {
        if (contract.getStatus() != TravelContractStatus.PENDING) {
            throw new CoreApiException(
                    CoreApiErrorType.INVALID_CONTRACT_REQUEST,
                    "계약 상태는 PENDING 이어야 합니다. 현재 상태=" + contract.getStatus());
        }
    }

    public void requireNotCanceled(TravelInsurePaymentEntity payment) {
        if (payment.getStatus() != TravelPaymentStatus.COMPLETED) {
            throw new CoreApiException(
                    CoreApiErrorType.INVALID_CONTRACT_REQUEST,
                    "결제 상태는 COMPLETED 이어야 합니다. 현재 상태=" + payment.getStatus());
        }
    }

    public static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new CoreApiException(CoreApiErrorType.INVALID_REQUEST, message);
        }
    }
}

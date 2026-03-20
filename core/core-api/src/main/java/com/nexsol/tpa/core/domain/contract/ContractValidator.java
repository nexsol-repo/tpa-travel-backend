package com.nexsol.tpa.core.domain.contract;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.enums.TravelContractStatus;
import com.nexsol.tpa.core.enums.TravelPaymentStatus;
import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelPaymentEntity;

@Component
public class ContractValidator {

    public void requirePending(TravelContractEntity contract) {
        if (contract.getStatus() != TravelContractStatus.PENDING) {
            throw new CoreApiException(
                    CoreApiErrorType.INVALID_CONTRACT_REQUEST,
                    "계약 상태는 PENDING 이어야 합니다. 현재 상태=" + contract.getStatus());
        }
    }

    /**
     * 취소 가능 상태를 검증한다.
     * - CANCELED → 이미 취소됨 (멱등성, 통과)
     * - COMPLETED → 취소 가능
     * - 그 외 → 예외
     */
    public void requireCancelable(TravelPaymentEntity payment) {
        TravelPaymentStatus status = payment.getStatus();
        if (status == TravelPaymentStatus.CANCELED || status == TravelPaymentStatus.COMPLETED) {
            return;
        }
        throw new CoreApiException(
                CoreApiErrorType.INVALID_CONTRACT_REQUEST,
                "결제 상태는 COMPLETED 또는 CANCELED 이어야 합니다. 현재 상태=" + status);
    }

    public boolean isAlreadyCanceled(TravelPaymentEntity payment) {
        return payment.getStatus() == TravelPaymentStatus.CANCELED;
    }

    public static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new CoreApiException(CoreApiErrorType.INVALID_REQUEST, message);
        }
    }
}

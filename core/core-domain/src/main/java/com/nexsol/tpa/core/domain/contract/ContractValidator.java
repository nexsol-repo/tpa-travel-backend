package com.nexsol.tpa.core.domain.contract;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

@Component
public class ContractValidator {

    public void requirePending(ContractInfo contract) {
        if (!"PENDING".equals(contract.status())) {
            throw new CoreException(
                    CoreErrorType.INVALID_CONTRACT_REQUEST,
                    "계약 상태는 PENDING 이어야 합니다. 현재 상태=" + contract.status());
        }
    }

    public void requireCancelable(Payment payment) {
        String status = payment.status();
        if ("CANCELED".equals(status) || "COMPLETED".equals(status)) {
            return;
        }
        throw new CoreException(
                CoreErrorType.INVALID_CONTRACT_REQUEST,
                "결제 상태는 COMPLETED 또는 CANCELED 이어야 합니다. 현재 상태=" + status);
    }

    public boolean isAlreadyCanceled(Payment payment) {
        return "CANCELED".equals(payment.status());
    }

    public static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new CoreException(CoreErrorType.INVALID_REQUEST, message);
        }
    }
}

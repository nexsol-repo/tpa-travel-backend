package com.nexsol.tpa.core.api.controller.v1.request;

import com.nexsol.tpa.core.domain.refund.RefundCommand;
import com.nexsol.tpa.core.enums.TravelPaymentMethod;

public record ContractCancelRequest(
        Long contractId,
        TravelPaymentMethod refundMethod,
        String bankName,
        String accountNumber,
        String depositorName,
        String refundReason) {

    public RefundCommand toRefundCommand() {
        return new RefundCommand(
                contractId,
                null,
                null,
                refundMethod,
                bankName,
                accountNumber,
                depositorName,
                refundReason);
    }
}

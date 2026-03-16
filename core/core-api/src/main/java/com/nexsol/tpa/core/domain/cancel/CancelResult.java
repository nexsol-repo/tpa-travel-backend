package com.nexsol.tpa.core.domain.cancel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CancelResult(ContractInfo contract) {

    public record ContractInfo(
            Long id,
            String status,
            String policyNumber,
            String meritzQuoteGroupNumber,
            String meritzQuoteRequestNumber,
            String countryName,
            String countryCode,
            Integer insuredPeopleNumber,
            BigDecimal totalFee,
            LocalDate insureBeginDate,
            LocalDate insureEndDate,
            InsurerInfo insurer,
            PlanInfo plan,
            PaymentInfo payment,
            BigDecimal refundAmount) {}

    public record InsurerInfo(Long id, String insurerCode, String insurerName) {}

    public record PlanInfo(
            Long id,
            String insuranceProductName,
            String planName,
            String productCode,
            String unitProductCode,
            String planGroupCode,
            String planCode) {}

    public record PaymentInfo(
            Long id,
            String paymentMethod,
            String status,
            BigDecimal paidAmount,
            LocalDateTime paymentDate,
            LocalDateTime cancelDate,
            String cardCompanyName) {}
}

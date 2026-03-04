package com.nexsol.tpa.core.api.dto.v1.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ContractCancelResponse {

    private Contract contract;

    @Getter
    @AllArgsConstructor
    public static class Contract {

        private Long id;

        private String status; // COMPLETED(계약) 그대로거나, 너희가 계약 취소 상태를 따로 안두면 그대로

        private String policyNumber;

        private String meritzQuoteGroupNumber;

        private String meritzQuoteRequestNumber;

        private String countryName;

        private String countryCode;

        private Integer insuredPeopleNumber;

        private BigDecimal totalFee;

        private LocalDate insureBeginDate;

        private LocalDate insureEndDate;

        private Insurer insurer;

        private Plan plan;

        private Payment payment;

        private BigDecimal refundAmount;

    }

    @Getter
    @AllArgsConstructor
    public static class Insurer {

        private Long id;

        private String insurerCode;

        private String insurerName;

    }

    @Getter
    @AllArgsConstructor
    public static class Plan {

        private Long id;

        private String insuranceProductName;

        private String planName;

        private String productCode;

        private String unitProductCode;

        private String planGroupCode;

        private String planCode;

    }

    @Getter
    @AllArgsConstructor
    public static class Payment {

        private Long id;

        private String paymentMethod; // CARD

        private String status; // COMPLETED / CANCELED

        private BigDecimal paidAmount; // 결제금액

        private LocalDateTime paymentDate;

        private LocalDateTime cancelDate;

        // UI에서 카드사 표기 필요하면(현재 DDL엔 없음) → 우선 null 내려도 됨
        private String cardCompanyName;

    }

}

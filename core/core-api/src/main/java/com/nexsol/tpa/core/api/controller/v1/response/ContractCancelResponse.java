package com.nexsol.tpa.core.api.controller.v1.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.nexsol.tpa.core.domain.cancel.CancelResult;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContractCancelResponse {

    private Contract contract;

    public static ContractCancelResponse of(CancelResult r) {
        var c = r.contract();
        return ContractCancelResponse.builder()
                .contract(
                        Contract.builder()
                                .id(c.id())
                                .status(c.status())
                                .policyNumber(c.policyNumber())
                                .meritzQuoteGroupNumber(c.meritzQuoteGroupNumber())
                                .meritzQuoteRequestNumber(c.meritzQuoteRequestNumber())
                                .countryName(c.countryName())
                                .countryCode(c.countryCode())
                                .insuredPeopleNumber(c.insuredPeopleNumber())
                                .totalFee(c.totalFee())
                                .insureBeginDate(c.insureBeginDate())
                                .insureEndDate(c.insureEndDate())
                                .insurer(
                                        Insurer.builder()
                                                .id(c.insurer().id())
                                                .insurerCode(c.insurer().insurerCode())
                                                .insurerName(c.insurer().insurerName())
                                                .build())
                                .plan(
                                        Plan.builder()
                                                .id(c.plan().id())
                                                .insuranceProductName(
                                                        c.plan().insuranceProductName())
                                                .planName(c.plan().planName())
                                                .productCode(c.plan().productCode())
                                                .unitProductCode(c.plan().unitProductCode())
                                                .planGroupCode(c.plan().planGroupCode())
                                                .planCode(c.plan().planCode())
                                                .build())
                                .payment(
                                        Payment.builder()
                                                .id(c.payment().id())
                                                .paymentMethod(c.payment().paymentMethod())
                                                .status(c.payment().status())
                                                .paidAmount(c.payment().paidAmount())
                                                .paymentDate(c.payment().paymentDate())
                                                .cancelDate(c.payment().cancelDate())
                                                .cardCompanyName(c.payment().cardCompanyName())
                                                .build())
                                .refundAmount(c.refundAmount())
                                .build())
                .build();
    }

    @Getter
    @Builder
    public static class Contract {

        private Long id;

        private String status;

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
    @Builder
    public static class Insurer {

        private Long id;

        private String insurerCode;

        private String insurerName;
    }

    @Getter
    @Builder
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
    @Builder
    public static class Payment {

        private Long id;

        private String paymentMethod;

        private String status;

        private BigDecimal paidAmount;

        private LocalDateTime paymentDate;

        private LocalDateTime cancelDate;

        private String cardCompanyName;
    }
}

package com.nexsol.tpa.core.api.controller.v1.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nexsol.tpa.core.domain.subscription.SubscriptionResult;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContractCompletedResponse {

    private final boolean ok;

    private final String errCd;

    private final String errMsg;

    private final Contract contract;

    private final Insurer insurer;

    private final Plan plan;

    public static ContractCompletedResponse of(SubscriptionResult r) {
        if (!r.ok()) {
            return ContractCompletedResponse.builder()
                    .ok(false)
                    .errCd(r.errCd())
                    .errMsg(r.errMsg())
                    .build();
        }
        var c = r.contract();
        var i = r.insurer();
        var p = r.plan();
        return ContractCompletedResponse.builder()
                .ok(true)
                .errCd(r.errCd())
                .errMsg(r.errMsg())
                .contract(
                        Contract.builder()
                                .id(c.id())
                                .partnerId(c.partnerId())
                                .channelId(c.channelId())
                                .planId(c.planId())
                                .familyId(c.familyId())
                                .policyNumber(c.policyNumber())
                                .meritzQuoteGroupNumber(c.meritzQuoteGroupNumber())
                                .meritzQuoteRequestNumber(c.meritzQuoteRequestNumber())
                                .countryName(c.countryName())
                                .countryCode(c.countryCode())
                                .totalFee(c.totalFee())
                                .status(c.status())
                                .insureBeginDate(c.insureBeginDate())
                                .insureEndDate(c.insureEndDate())
                                .contractPeopleName(c.contractPeopleName())
                                .contractPeopleHp(c.contractPeopleHp())
                                .contractPeopleMail(c.contractPeopleMail())
                                .build())
                .insurer(Insurer.builder().id(i.id()).name(i.name()).code(i.code()).build())
                .plan(
                        Plan.builder()
                                .id(p.id())
                                .insuranceProductName(p.insuranceProductName())
                                .planName(p.planName())
                                .productCode(p.productCode())
                                .unitProductCode(p.unitProductCode())
                                .planGroupCode(p.planGroupCode())
                                .planCode(p.planCode())
                                .build())
                .build();
    }

    @Getter
    @Builder
    public static class Contract {

        private Long id;

        private Long partnerId;

        private Long channelId;

        private Long planId;

        private Long familyId;

        private String policyNumber;

        private String meritzQuoteGroupNumber;

        private String meritzQuoteRequestNumber;

        private String countryName;

        private String countryCode;

        private BigDecimal totalFee;

        private String status;

        private LocalDate insureBeginDate;

        private LocalDate insureEndDate;

        private String contractPeopleName;

        private String contractPeopleHp;

        private String contractPeopleMail;
    }

    @Getter
    @Builder
    public static class Insurer {

        private Long id;

        private String name;

        private String code;
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
}

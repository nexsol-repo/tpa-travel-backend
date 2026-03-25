package com.nexsol.tpa.core.api.controller.v1.response;

import static com.nexsol.tpa.core.support.MaskingUtils.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.nexsol.tpa.core.domain.contract.*;
import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.plan.Insurer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContractDetailResponse {

    // 계약 정보
    private Long contractId;
    private String policyNumber;
    private BigDecimal totalPremium;
    private String status;
    private LocalDateTime applyDate;
    private String termsUrl;
    private String policyLink;

    // 보험기간
    private LocalDate insureStartDate;
    private LocalDate insureEndDate;
    private String countryCode;
    private String countryName;

    // 견적
    private String quoteGroupNumber;
    private String quoteRequestNumber;

    // 보험사
    private Long insurerId;
    private String insurerName;

    // 제휴사
    private Long partnerId;
    private String partnerName;

    // 채널
    private Long channelId;
    private String channelName;

    // 플랜
    private Long planId;
    private String planName;
    private String planCode;

    // 결제
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal paidAmount;
    private LocalDateTime paymentDate;

    // 피보험자
    private List<InsuredPersonDetail> people;

    private static final String TERMS_URL =
            "https://filer.bucket.nexsol.ai/buckets/tpa-travel-dev/insurance/term.pdf";

    public static ContractDetailResponse of(
            ContractInfo contract,
            Payment payment,
            List<InsuredPerson> people,
            InsurancePlan plan,
            Insurer insurer,
            Partner partner,
            Channel channel) {

        return ContractDetailResponse.builder()
                .contractId(contract.id())
                .policyNumber(contract.policyNumber())
                .totalPremium(contract.totalPremium())
                .status(contract.status())
                .applyDate(contract.applyDate())
                .termsUrl(TERMS_URL)
                .policyLink(contract.policyLink())
                .insureStartDate(
                        contract.insurePeriod() != null
                                ? contract.insurePeriod().startDate()
                                : null)
                .insureEndDate(
                        contract.insurePeriod() != null ? contract.insurePeriod().endDate() : null)
                .countryCode(
                        contract.insurePeriod() != null
                                ? contract.insurePeriod().countryCode()
                                : null)
                .countryName(
                        contract.insurePeriod() != null
                                ? contract.insurePeriod().countryName()
                                : null)
                .quoteGroupNumber(contract.quote() != null ? contract.quote().groupNumber() : null)
                .quoteRequestNumber(
                        contract.quote() != null ? contract.quote().requestNumber() : null)
                .insurerId(insurer != null ? insurer.id() : null)
                .insurerName(insurer != null ? insurer.name() : null)
                .partnerId(partner != null ? partner.id() : null)
                .partnerName(partner != null ? partner.name() : null)
                .channelId(channel != null ? channel.id() : null)
                .channelName(channel != null ? channel.name() : null)
                .planId(plan != null ? plan.id() : null)
                .planName(plan != null ? plan.planName() : null)
                .planCode(plan != null ? plan.planCode() : null)
                .paymentMethod(payment != null ? payment.paymentMethod() : null)
                .paymentStatus(payment != null ? payment.status() : null)
                .paidAmount(payment != null ? payment.paidAmount() : null)
                .paymentDate(payment != null ? payment.paymentDate() : null)
                .people(
                        people.stream()
                                .map(
                                        p ->
                                                InsuredPersonDetail.builder()
                                                        .id(p.id())
                                                        .planId(p.planId())
                                                        .isContractor(p.isContractor())
                                                        .name(p.name())
                                                        .englishName(p.englishName())
                                                        .gender(p.gender())
                                                        .residentNumberMasked(
                                                                maskRrn(p.residentNumber()))
                                                        .passportNumberMasked(
                                                                maskPassport(p.passportNumber()))
                                                        .phone(p.phone())
                                                        .email(p.email())
                                                        .insurePremium(p.insurePremium())
                                                        .build())
                                .toList())
                .build();
    }
}

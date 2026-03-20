package com.nexsol.tpa.core.api.controller.v1.response;

import static com.nexsol.tpa.core.support.MaskingUtils.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.nexsol.tpa.core.domain.contract.*;
import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.plan.Insurer;
import com.nexsol.tpa.storage.db.core.entity.*;

import lombok.Builder;

public final class ContractQueryResponse {

    private static final String TERMS_URL =
            "https://filer.bucket.nexsol.ai/buckets/tpa-travel-dev/insurance/term.pdf";

    private ContractQueryResponse() {}

    // ── 목록 조회 ──

    @Builder
    public record ContractListItem(
            Long id,
            String policyNumber,
            BigDecimal totalPremium,
            String status,
            int insuredPeopleNumber,
            LocalDateTime applyDate,
            String termsUrl,
            String policyLink,
            Insurer insurer,
            Partner partner,
            Channel channel,
            InsurancePlan plan,
            InsurePeriod insurePeriod,
            AuthInfo auth,
            Contractor contractor,
            Payment payment,
            List<PersonSummary> people) {

        public static ContractListItem of(
                TravelContractEntity c,
                TravelPaymentEntity pay,
                TravelInsurancePlanEntity plan,
                List<TravelInsuredEntity> people) {

            var contractorEntity =
                    people.stream()
                            .filter(p -> Boolean.TRUE.equals(p.getIsContractor()))
                            .findFirst()
                            .orElse(null);

            return ContractListItem.builder()
                    .id(c.getId())
                    .policyNumber(c.getPolicyNumber())
                    .totalPremium(c.getTotalPremium())
                    .status(c.getStatus() != null ? c.getStatus().name() : null)
                    .insuredPeopleNumber(people.size())
                    .applyDate(c.getApplyDate())
                    .termsUrl(TERMS_URL)
                    .policyLink(c.getPolicyLink())
                    .insurer(
                            Insurer.builder().id(c.getInsurerId()).name(c.getInsurerName()).build())
                    .partner(
                            Partner.builder().id(c.getPartnerId()).name(c.getPartnerName()).build())
                    .channel(
                            Channel.builder().id(c.getChannelId()).name(c.getChannelName()).build())
                    .plan(toInsurancePlan(plan))
                    .insurePeriod(toInsurePeriod(c))
                    .auth(toAuthInfo(c))
                    .contractor(toContractor(contractorEntity))
                    .payment(toPayment(pay))
                    .people(
                            people.stream()
                                    .map(p -> new PersonSummary(p.getId(), p.getName()))
                                    .toList())
                    .build();
        }
    }

    // ── 상세 조회 ──

    @Builder
    public record ContractDetail(
            ContractInfo contract,
            Insurer insurer,
            Partner partner,
            Channel channel,
            InsurancePlan plan,
            Payment payment,
            String termsUrl,
            String policyLink,
            List<InsuredPerson> people) {

        public static ContractDetail of(
                TravelContractEntity c,
                TravelPaymentEntity pay,
                List<TravelInsuredEntity> people,
                TravelInsurancePlanEntity plan,
                TravelInsurerEntity insurer,
                TpaPartnerEntity partner,
                TpaChannelEntity channel) {

            var contractorEntity =
                    people.stream()
                            .filter(p -> Boolean.TRUE.equals(p.getIsContractor()))
                            .findFirst()
                            .orElse(null);

            return ContractDetail.builder()
                    .contract(toContractInfo(c, toContractor(contractorEntity)))
                    .insurer(toInsurer(insurer))
                    .partner(toPartner(partner))
                    .channel(toChannel(channel))
                    .plan(toInsurancePlan(plan))
                    .payment(toPayment(pay))
                    .termsUrl(TERMS_URL)
                    .policyLink(c.getPolicyLink())
                    .people(people.stream().map(ContractQueryResponse::toInsuredPerson).toList())
                    .build();
        }
    }

    // ── Entity → 도메인 매핑 ──

    private static ContractInfo toContractInfo(TravelContractEntity c, Contractor contractor) {
        return ContractInfo.builder()
                .id(c.getId())
                .familyId(c.getFamilyId())
                .policyNumber(c.getPolicyNumber())
                .meritzQuoteGroupNumber(c.getMeritzQuoteGroupNumber())
                .meritzQuoteRequestNumber(c.getMeritzQuoteRequestNumber())
                .totalPremium(c.getTotalPremium())
                .policyLink(c.getPolicyLink())
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .applyDate(c.getApplyDate())
                .insurePeriod(toInsurePeriod(c))
                .contractor(contractor)
                .auth(toAuthInfo(c))
                .marketingConsentUsed(Boolean.TRUE.equals(c.getMarketingConsentUsed()))
                .employeeId(c.getEmployeeId())
                .build();
    }

    private static Contractor toContractor(TravelInsuredEntity insured) {
        if (insured == null) return null;
        return Contractor.builder()
                .name(insured.getName())
                .residentNumberMasked(maskRrn(insured.getResidentNumber()))
                .phone(insured.getPhone())
                .email(insured.getEmail())
                .build();
    }

    private static InsurePeriod toInsurePeriod(TravelContractEntity c) {
        return InsurePeriod.builder()
                .startDate(c.getInsureStartDate())
                .endDate(c.getInsureEndDate())
                .countryCode(c.getCountryCode())
                .countryName(c.getCountryName())
                .build();
    }

    private static AuthInfo toAuthInfo(TravelContractEntity c) {
        return AuthInfo.builder()
                .uniqueKey(c.getAuthUniqueKey())
                .status(c.getAuthStatus())
                .date(c.getAuthDate())
                .build();
    }

    private static InsuredPerson toInsuredPerson(TravelInsuredEntity e) {
        return InsuredPerson.builder()
                .id(e.getId())
                .planId(e.getPlanId())
                .isContractor(Boolean.TRUE.equals(e.getIsContractor()))
                .name(e.getName())
                .englishName(e.getEnglishName())
                .gender(e.getGender())
                .residentNumberMasked(maskRrn(e.getResidentNumber()))
                .passportNumberMasked(maskPassport(e.getPassportNumber()))
                .insurePremium(e.getInsurePremium())
                .build();
    }

    private static Payment toPayment(TravelPaymentEntity e) {
        if (e == null) return null;
        return Payment.builder()
                .id(e.getId())
                .paymentMethod(e.getPaymentMethod() != null ? e.getPaymentMethod().name() : null)
                .status(e.getStatus() != null ? e.getStatus().name() : null)
                .paidAmount(e.getPaidAmount())
                .paymentDate(e.getPaymentDate())
                .cancelDate(e.getCancelDate())
                .build();
    }

    private static InsurancePlan toInsurancePlan(TravelInsurancePlanEntity e) {
        if (e == null) return null;
        return InsurancePlan.builder()
                .id(e.getId())
                .insuranceProductName(e.getInsuranceProductName())
                .planName(toDisplayName(e.getPlanFullName(), e.getPlanName()))
                .planFullName(e.getPlanFullName())
                .productCode(e.getProductCode())
                .unitProductCode(e.getUnitProductCode())
                .planGroupCode(e.getPlanGroupCode())
                .planCode(e.getPlanCode())
                .build();
    }

    private static Insurer toInsurer(TravelInsurerEntity e) {
        if (e == null) return null;
        return Insurer.builder()
                .id(e.getId())
                .code(e.getInsurerCode())
                .name(e.getInsurerName())
                .build();
    }

    private static Partner toPartner(TpaPartnerEntity e) {
        if (e == null) return null;
        return Partner.builder()
                .id(e.getId())
                .code(e.getPartnerCode())
                .name(e.getPartnerName())
                .build();
    }

    private static Channel toChannel(TpaChannelEntity e) {
        if (e == null) return null;
        return Channel.builder()
                .id(e.getId())
                .code(e.getChannelCode())
                .name(e.getChannelName())
                .build();
    }

    private static String toDisplayName(String planFullName, String planName) {
        String name = planFullName != null ? planFullName : planName;
        if (name == null) return null;
        return name.replace("플랜A", "플랜")
                .replace("플랜B", "플랜")
                .replace(" 실손제외", "(실손제외)")
                .replaceAll("_\\d+~\\d+세$", "")
                .trim();
    }
}

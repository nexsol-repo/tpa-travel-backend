package com.nexsol.tpa.core.api.controller.v1.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.nexsol.tpa.core.domain.contract.vo.*;
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
                TravelInsurePaymentEntity pay,
                TravelInsurancePlanEntity plan,
                List<TravelInsurePeopleEntity> people) {

            return ContractListItem.builder()
                    .id(c.getId())
                    .policyNumber(c.getPolicyNumber())
                    .totalPremium(c.getTotalPremium())
                    .status(c.getStatus() != null ? c.getStatus().name() : null)
                    .insuredPeopleNumber(people.size())
                    .applyDate(c.getApplyDate())
                    .termsUrl(TERMS_URL)
                    .policyLink(c.getPolicyLink())
                    .insurer(Insurer.builder()
                            .id(c.getInsurerId())
                            .name(c.getInsurerName())
                            .build())
                    .partner(Partner.builder()
                            .id(c.getPartnerId())
                            .name(c.getPartnerName())
                            .build())
                    .channel(Channel.builder()
                            .id(c.getChannelId())
                            .name(c.getChannelName())
                            .build())
                    .plan(InsurancePlan.of(plan))
                    .insurePeriod(InsurePeriod.of(c))
                    .auth(AuthInfo.of(c))
                    .contractor(Contractor.of(c))
                    .payment(Payment.of(pay))
                    .people(people.stream()
                            .map(p -> new PersonSummary(p.getId(), p.getName()))
                            .toList())
                    .build();
        }
    }

    public record PersonSummary(Long id, String name) {}

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
                TravelInsurePaymentEntity pay,
                List<TravelInsurePeopleEntity> people,
                TravelInsurancePlanEntity plan,
                TravelInsurerEntity insurer,
                TpaPartnerEntity partner,
                TpaChannelEntity channel) {

            return ContractDetail.builder()
                    .contract(ContractInfo.of(c))
                    .insurer(Insurer.of(insurer))
                    .partner(Partner.of(partner))
                    .channel(Channel.of(channel))
                    .plan(InsurancePlan.of(plan))
                    .payment(Payment.of(pay))
                    .termsUrl(TERMS_URL)
                    .policyLink(c.getPolicyLink())
                    .people(people.stream().map(InsuredPerson::of).toList())
                    .build();
        }
    }

    // ── 상세 조회 전용: 계약 정보 ──

    @Builder
    public record ContractInfo(
            Long id,
            Long familyId,
            String policyNumber,
            String meritzQuoteGroupNumber,
            String meritzQuoteRequestNumber,
            BigDecimal totalPremium,
            String policyLink,
            String status,
            LocalDateTime applyDate,
            InsurePeriod insurePeriod,
            Contractor contractor,
            AuthInfo auth,
            boolean marketingConsentUsed,
            Long employeeId) {

        public static ContractInfo of(TravelContractEntity c) {
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
                    .insurePeriod(InsurePeriod.of(c))
                    .contractor(Contractor.of(c))
                    .auth(AuthInfo.of(c))
                    .marketingConsentUsed(Boolean.TRUE.equals(c.getMarketingConsentUsed()))
                    .employeeId(c.getEmployeeId())
                    .build();
        }
    }
}
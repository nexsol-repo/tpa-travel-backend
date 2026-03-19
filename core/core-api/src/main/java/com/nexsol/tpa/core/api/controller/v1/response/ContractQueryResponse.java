package com.nexsol.tpa.core.api.controller.v1.response;

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
                    .plan(InsurancePlan.of(plan))
                    .insurePeriod(InsurePeriod.of(c))
                    .auth(AuthInfo.of(c))
                    .contractor(Contractor.of(c))
                    .payment(Payment.of(pay))
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
}

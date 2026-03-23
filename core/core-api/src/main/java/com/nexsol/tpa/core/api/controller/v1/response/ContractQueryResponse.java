package com.nexsol.tpa.core.api.controller.v1.response;

import static com.nexsol.tpa.core.support.MaskingUtils.*;

import java.util.List;

import com.nexsol.tpa.core.domain.contract.*;
import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.plan.Insurer;

public final class ContractQueryResponse {

    private static final String TERMS_URL =
            "https://filer.bucket.nexsol.ai/buckets/tpa-travel-dev/insurance/term.pdf";

    private ContractQueryResponse() {}

    public static ContractListItem of(
            ContractInfo c, Payment payment, InsurancePlan plan, List<InsuredPerson> people) {

        var contractor =
                people.stream()
                        .filter(InsuredPerson::isContractor)
                        .findFirst()
                        .map(
                                p ->
                                        Contractor.builder()
                                                .name(p.name())
                                                .residentNumberMasked(maskRrn(p.residentNumber()))
                                                .phone(p.phone())
                                                .email(p.email())
                                                .build())
                        .orElse(null);

        return ContractListItem.builder()
                .id(c.id())
                .policyNumber(c.policyNumber())
                .totalPremium(c.totalPremium())
                .status(c.status())
                .insuredPeopleNumber(people.size())
                .applyDate(c.applyDate())
                .termsUrl(TERMS_URL)
                .policyLink(c.policyLink())
                .insurer(Insurer.builder().id(c.insurerId()).name(c.insurerName()).build())
                .partner(Partner.builder().id(c.partnerId()).name(c.partnerName()).build())
                .channel(Channel.builder().id(c.channelId()).name(c.channelName()).build())
                .plan(plan)
                .insurePeriod(c.insurePeriod())
                .auth(c.auth())
                .contractor(contractor)
                .payment(payment)
                .people(people.stream().map(p -> new PersonSummary(p.id(), p.name())).toList())
                .build();
    }

    public static ContractDetail of(
            ContractInfo contract,
            Payment payment,
            List<InsuredPerson> people,
            InsurancePlan plan,
            Insurer insurer,
            Partner partner,
            Channel channel) {

        return ContractDetail.builder()
                .contract(contract)
                .insurer(insurer)
                .partner(partner)
                .channel(channel)
                .plan(plan)
                .payment(payment)
                .termsUrl(TERMS_URL)
                .policyLink(contract.policyLink())
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

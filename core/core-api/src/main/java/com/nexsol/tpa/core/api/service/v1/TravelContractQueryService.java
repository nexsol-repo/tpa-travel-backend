package com.nexsol.tpa.core.api.service.v1;

import com.nexsol.tpa.core.api.entity.*;
import com.nexsol.tpa.core.api.repository.v1.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.nexsol.tpa.core.api.dto.v1.contract.TravelContractQueryDto.*;

@Service
@RequiredArgsConstructor
public class TravelContractQueryService {

    private final TravelContractRepository contractRepository;

    private final TravelInsurePaymentRepository paymentRepository;

    private final TravelInsurancePlanRepository planRepository;

    private final TravelInsurerRepository insurerRepository;

    private final TpaPartnerRepository partnerRepository;

    private final TpaChannelRepository channelRepository;

    private final TravelInsurePeopleRepository peopleRepository;

    @Transactional(readOnly = true)
    public Page<ContractListItem> list(String authUniqueKey, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));

        Page<TravelContractEntity> contracts = (authUniqueKey != null && !authUniqueKey.isBlank())
                ? contractRepository.findByAuthUniqueKey(authUniqueKey, pageable)
                : contractRepository.findAllOrderByAuthUniqueKeyDesc(pageable);

        List<Long> contractIds = contracts.getContent().stream().map(TravelContractEntity::getId).toList();
        List<Long> planIds = contracts.getContent()
            .stream()
            .map(TravelContractEntity::getPlanId)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .toList();

        var payMap = paymentRepository.findByContractIdIn(contractIds)
            .stream()
            .collect(java.util.stream.Collectors.toMap(TravelInsurePaymentEntity::getContractId, p -> p));

        var planMap = planRepository.findByIdIn(planIds)
            .stream()
            .collect(java.util.stream.Collectors.toMap(TravelInsurancePlanEntity::getId, p -> p));

        var peopleMap = peopleRepository.findByContractIds(contractIds)
            .stream()
            .collect(java.util.stream.Collectors.groupingBy(p -> p.getContract().getId()));

        return contracts.map(c -> {
            var pay = payMap.get(c.getId());
            var plan = planMap.get(c.getPlanId());
            var people = peopleMap.getOrDefault(c.getId(), java.util.List.of());

            return ContractListItem.builder()
                .id(c.getId())
                .insurerId(c.getInsurerId())
                .insurerName(c.getInsurerName())
                .partnerId(c.getPartnerId())
                .partnerName(c.getPartnerName())
                .channelId(c.getChannelId())
                .channelName(c.getChannelName())
                .planId(c.getPlanId())
                .planName(plan != null ? plan.getPlanName() : null)
                .policyNumber(c.getPolicyNumber())
                .countryName(c.getCountryName())
                .countryCode(c.getCountryCode())
                .insuredPeopleNumber(c.getInsuredPeopleNumber())
                .totalPremium(c.getTotalPremium())
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .authUniqueKey(c.getAuthUniqueKey())
                .authStatus(c.getAuthStatus())
                .authDate(c.getAuthDate())
                .applyDate(c.getApplyDate())
                .insureStartDate(c.getInsureStartDate())
                .insureEndDate(c.getInsureEndDate())
                // TODO: change to hard coding
                .termsUrl("https://filer.bucket.nexsol.ai/buckets/tpa-travel-dev/insurance/term.pdf")
                .policyLink(c.getPolicyLink())

                .contractPeopleName(c.getContractPeopleName())
                .contractPeopleResidentNumberMasked(maskRrn(c.getContractPeopleResidentNumber()))
                .contractPeopleHp(c.getContractPeopleHp())
                .contractPeopleMail(c.getContractPeopleMail())

                .payment(toPayment(pay))
                .people(people.stream()
                    .map(p -> PersonSummary.builder().id(p.getId()).name(p.getName()).build())
                    .toList())
                .build();
        });
    }

    @Transactional(readOnly = true)
    public ContractDetail get(Long id) {

        TravelContractEntity c = contractRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("contract not found: " + id));

        TravelInsurePaymentEntity pay = paymentRepository.findByContractId(c.getId()).orElse(null);

        TravelInsurancePlanEntity plan = planRepository.findById(c.getPlanId()).orElse(null);

        TravelInsurerEntity insurer = insurerRepository.findById(c.getInsurerId()).orElse(null);

        TpaPartnerEntity partner = partnerRepository.findById(c.getPartnerId()).orElse(null);

        TpaChannelEntity channel = channelRepository.findById(c.getChannelId()).orElse(null);

        return ContractDetail.builder()
            .contract(toContract(c))
            .insurer(insurer != null ? Insurer.builder()
                .id(insurer.getId())
                .insurerCode(insurer.getInsurerCode())
                .insurerName(insurer.getInsurerName())
                .build() : null)

            .partner(partner != null ? Partner.builder()
                .id(partner.getId())
                .partnerCode(partner.getPartnerCode())
                .partnerName(partner.getPartnerName())
                .build() : null)

            .channel(channel != null ? Channel.builder()
                .id(channel.getId())
                .channelCode(channel.getChannelCode())
                .channelName(channel.getChannelName())
                .build() : null)

            .plan(plan != null ? Plan.builder()
                .id(plan.getId())
                .insuranceProductName(plan.getInsuranceProductName())
                .planName(plan.getPlanName())
                .planFullName(plan.getPlanFullName())
                .productCode(plan.getProductCode())
                .unitProductCode(plan.getUnitProductCode())
                .planGroupCode(plan.getPlanGroupCode())
                .planCode(plan.getPlanCode())
                .build() : null)

            .payment(toPayment(pay))
            .people(c.getPeople().stream().map(this::toPerson).toList())
            .termsUrl("https://filer.bucket.nexsol.ai/buckets/tpa-travel-dev/insurance/term.pdf")
            .policyLink(c.getPolicyLink())
            .build();
    }

    private Contract toContract(TravelContractEntity c) {
        return Contract.builder()
            .id(c.getId())
            .insurerId(c.getInsurerId())
            .partnerId(c.getPartnerId())
            .channelId(c.getChannelId())
            .planId(c.getPlanId())

            .insurerName(c.getInsurerName())
            .partnerName(c.getPartnerName())
            .channelName(c.getChannelName())

            .policyNumber(c.getPolicyNumber())
            .meritzQuoteGroupNumber(c.getMeritzQuoteGroupNumber())
            .meritzQuoteRequestNumber(c.getMeritzQuoteRequestNumber())

            .countryName(c.getCountryName())
            .countryCode(c.getCountryCode())

            .insuredPeopleNumber(c.getInsuredPeopleNumber())
            .totalPremium(c.getTotalPremium())

            .status(c.getStatus() != null ? c.getStatus().name() : null)

            .applyDate(c.getApplyDate())
            .insureStartDate(c.getInsureStartDate())
            .insureEndDate(c.getInsureEndDate())

            .contractPeopleName(c.getContractPeopleName())
            .contractPeopleResidentNumberMasked(maskRrn(c.getContractPeopleResidentNumber()))
            .contractPeopleHp(c.getContractPeopleHp())
            .contractPeopleMail(c.getContractPeopleMail())

            .marketingConsentUsed(Boolean.TRUE.equals(c.getMarketingConsentUsed()))

            .authProvider(c.getAuthProvider())
            .authImpUid(c.getAuthImpUid())
            .authRequestId(c.getAuthRequestId())
            .authUniqueKey(c.getAuthUniqueKey())
            .authStatus(c.getAuthStatus())
            .authDate(c.getAuthDate())

            .employeeId(c.getEmployeeId())
            // createdAt/updatedAt은 AuditEntity에 있을 가능성 높음. 있으면 아래 살려.
            // .createdAt(c.getCreatedAt())
            // .updatedAt(c.getUpdatedAt())
            .build();
    }

    private Person toPerson(TravelInsurePeopleEntity p) {
        return Person.builder()
            .id(p.getId())
            .name(p.getName())
            .nameEng(p.getNameEng())
            .gender(p.getGender())
            .residentNumberMasked(maskRrn(p.getResidentNumber()))
            .passportNumberMasked(maskPassport(p.getPassportNumber()))
            .policyNumber(p.getPolicyNumber())
            .insurePremium(p.getInsurePremium())
            .build();
    }

    private static Payment toPayment(TravelInsurePaymentEntity pay) {
        if (pay == null)
            return null;
        return Payment.builder()
            .id(pay.getId())
            .paymentMethod(pay.getPaymentMethod() != null ? pay.getPaymentMethod().name() : null)
            .status(pay.getStatus() != null ? pay.getStatus().name() : null)
            .paidAmount(pay.getPaidAmount())
            .paymentDate(pay.getPaymentDate())
            .cancelDate(pay.getCancelDate())
            .build();
    }

    private static String maskRrn(String rrn) {
        if (rrn == null || rrn.isBlank())
            return null;
        String digits = rrn.replaceAll("[^0-9]", "");
        if (digits.length() < 6)
            return "******-*******";
        return digits.substring(0, 6) + "-*******";
    }

    private static String maskPassport(String passport) {
        if (passport == null || passport.isBlank())
            return null;
        if (passport.length() <= 3)
            return "***";
        return passport.substring(0, 2) + "***" + passport.substring(passport.length() - 1);
    }

}

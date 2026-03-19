package com.nexsol.tpa.core.api.dto.v1.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.nexsol.tpa.storage.db.core.entity.*;

import lombok.*;

public class TravelContractQueryDto {

    private static final String TERMS_URL =
            "https://filer.bucket.nexsol.ai/buckets/tpa-travel-dev/insurance/term.pdf";

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractListItem {

        private Long id;

        private Long insurerId;

        private String insurerName;

        private Long partnerId;

        private String partnerName;

        private Long channelId;

        private String channelName;

        private Long planId;

        private Long familyId;

        private String planName;

        private String policyNumber;

        private String countryName;

        private String countryCode;

        private Integer insuredPeopleNumber;

        private BigDecimal totalPremium;

        private String status;

        private String authUniqueKey;

        private String authStatus;

        private LocalDateTime authDate;

        private LocalDateTime applyDate;

        private LocalDate insureStartDate;

        private LocalDate insureEndDate;

        private Payment payment;

        private List<PersonSummary> people;

        private String contractPeopleName;

        private String contractPeopleResidentNumberMasked;

        private String contractPeopleHp;

        private String contractPeopleMail;

        private String termsUrl;

        private String policyLink;

        public static ContractListItem of(
                TravelContractEntity c,
                TravelInsurePaymentEntity pay,
                TravelInsurancePlanEntity plan,
                List<TravelInsurePeopleEntity> people) {
            return ContractListItem.builder()
                    .id(c.getId())
                    .insurerId(c.getInsurerId())
                    .insurerName(c.getInsurerName())
                    .partnerId(c.getPartnerId())
                    .partnerName(c.getPartnerName())
                    .channelId(c.getChannelId())
                    .channelName(c.getChannelName())
                    .planId(c.getPlanId())
                    .familyId(c.getFamilyId())
                    .planName(
                            plan != null
                                    ? toDisplayName(plan.getPlanFullName(), plan.getPlanName())
                                    : null)
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
                    .termsUrl(TERMS_URL)
                    .policyLink(c.getPolicyLink())
                    .contractPeopleName(c.getContractPeopleName())
                    .contractPeopleResidentNumberMasked(
                            maskRrn(c.getContractPeopleResidentNumber()))
                    .contractPeopleHp(c.getContractPeopleHp())
                    .contractPeopleMail(c.getContractPeopleMail())
                    .payment(Payment.of(pay))
                    .people(
                            people.stream()
                                    .map(
                                            p ->
                                                    PersonSummary.builder()
                                                            .id(p.getId())
                                                            .name(p.getName())
                                                            .build())
                                    .toList())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PersonSummary {

        private Long id;

        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractDetail {

        private Contract contract;

        private Insurer insurer;

        private Partner partner;

        private Channel channel;

        private Plan plan;

        private Payment payment;

        private String termsUrl;

        private String policyLink;

        private List<Person> people;

        public static ContractDetail of(
                TravelContractEntity c,
                TravelInsurePaymentEntity pay,
                List<TravelInsurePeopleEntity> people,
                TravelInsurancePlanEntity plan,
                TravelInsurerEntity insurer,
                TpaPartnerEntity partner,
                TpaChannelEntity channel) {
            return ContractDetail.builder()
                    .contract(Contract.of(c))
                    .insurer(Insurer.of(insurer))
                    .partner(Partner.of(partner))
                    .channel(Channel.of(channel))
                    .plan(Plan.of(plan))
                    .payment(Payment.of(pay))
                    .people(people.stream().map(Person::of).toList())
                    .termsUrl(TERMS_URL)
                    .policyLink(c.getPolicyLink())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Contract {

        private Long id;

        private Long insurerId;

        private Long partnerId;

        private Long channelId;

        private Long planId;

        private Long familyId;

        private String insurerName;

        private String partnerName;

        private String channelName;

        private String policyNumber;

        private String meritzQuoteGroupNumber;

        private String meritzQuoteRequestNumber;

        private String countryName;

        private String countryCode;

        private Integer insuredPeopleNumber;

        private BigDecimal totalPremium;

        private String policyLink;

        private String status;

        private LocalDateTime applyDate;

        private LocalDate insureStartDate;

        private LocalDate insureEndDate;

        private String contractPeopleName;

        private String contractPeopleResidentNumberMasked;

        private String contractPeopleHp;

        private String contractPeopleMail;

        private boolean marketingConsentUsed;

        private String authProvider;

        private String authImpUid;

        private String authRequestId;

        private String authUniqueKey;

        private String authStatus;

        private LocalDateTime authDate;

        private Long employeeId;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        public static Contract of(TravelContractEntity c) {
            return Contract.builder()
                    .id(c.getId())
                    .insurerId(c.getInsurerId())
                    .partnerId(c.getPartnerId())
                    .channelId(c.getChannelId())
                    .planId(c.getPlanId())
                    .familyId(c.getFamilyId())
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
                    .policyLink(c.getPolicyLink())
                    .status(c.getStatus() != null ? c.getStatus().name() : null)
                    .applyDate(c.getApplyDate())
                    .insureStartDate(c.getInsureStartDate())
                    .insureEndDate(c.getInsureEndDate())
                    .contractPeopleName(c.getContractPeopleName())
                    .contractPeopleResidentNumberMasked(
                            maskRrn(c.getContractPeopleResidentNumber()))
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
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payment {

        private Long id;

        private String paymentMethod;

        private String status;

        private BigDecimal paidAmount;

        private LocalDateTime paymentDate;

        private LocalDateTime cancelDate;

        public static Payment of(TravelInsurePaymentEntity pay) {
            if (pay == null) return null;
            return Payment.builder()
                    .id(pay.getId())
                    .paymentMethod(
                            pay.getPaymentMethod() != null ? pay.getPaymentMethod().name() : null)
                    .status(pay.getStatus() != null ? pay.getStatus().name() : null)
                    .paidAmount(pay.getPaidAmount())
                    .paymentDate(pay.getPaymentDate())
                    .cancelDate(pay.getCancelDate())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Person {

        private Long id;

        private Long planId;

        private String name;

        private String nameEng;

        private String gender;

        private String residentNumberMasked;

        private String passportNumberMasked;

        private String policyNumber;

        private BigDecimal insurePremium;

        public static Person of(TravelInsurePeopleEntity p) {
            return Person.builder()
                    .id(p.getId())
                    .planId(p.getPlanId())
                    .name(p.getName())
                    .nameEng(p.getNameEng())
                    .gender(p.getGender())
                    .residentNumberMasked(maskRrn(p.getResidentNumber()))
                    .passportNumberMasked(maskPassport(p.getPassportNumber()))
                    .policyNumber(p.getPolicyNumber())
                    .insurePremium(p.getInsurePremium())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Insurer {

        private Long id;

        private String insurerCode;

        private String insurerName;

        public static Insurer of(TravelInsurerEntity e) {
            if (e == null) return null;
            return Insurer.builder()
                    .id(e.getId())
                    .insurerCode(e.getInsurerCode())
                    .insurerName(e.getInsurerName())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Partner {

        private Long id;

        private String partnerCode;

        private String partnerName;

        public static Partner of(TpaPartnerEntity e) {
            if (e == null) return null;
            return Partner.builder()
                    .id(e.getId())
                    .partnerCode(e.getPartnerCode())
                    .partnerName(e.getPartnerName())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Channel {

        private Long id;

        private String channelCode;

        private String channelName;

        public static Channel of(TpaChannelEntity e) {
            if (e == null) return null;
            return Channel.builder()
                    .id(e.getId())
                    .channelCode(e.getChannelCode())
                    .channelName(e.getChannelName())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Plan {

        private Long id;

        private String insuranceProductName;

        private String planName;

        private String planFullName;

        private String productCode;

        private String unitProductCode;

        private String planGroupCode;

        private String planCode;

        public static Plan of(TravelInsurancePlanEntity e) {
            if (e == null) return null;
            return Plan.builder()
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
    }

    // ── Display Name ──

    private static String toDisplayName(String planFullName, String planName) {
        String name = planFullName != null ? planFullName : planName;
        if (name == null) return null;
        return name.replace("플랜A", "플랜")
                .replace("플랜B", "플랜")
                .replace(" 실손제외", "(실손제외)")
                .replaceAll("_\\d+~\\d+세$", "")
                .trim();
    }

    // ── Masking Utilities ──

    private static String maskRrn(String rrn) {
        if (rrn == null || rrn.isBlank()) return null;
        String digits = rrn.replaceAll("[^0-9]", "");
        if (digits.length() < 6) return "******-*******";
        return digits.substring(0, 6) + "-*******";
    }

    private static String maskPassport(String passport) {
        if (passport == null || passport.isBlank()) return null;
        if (passport.length() <= 3) return "***";
        return passport.substring(0, 2) + "***" + passport.substring(passport.length() - 1);
    }
}

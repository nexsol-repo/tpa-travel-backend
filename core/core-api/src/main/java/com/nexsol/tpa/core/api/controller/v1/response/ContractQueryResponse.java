package com.nexsol.tpa.core.api.controller.v1.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.nexsol.tpa.storage.db.core.entity.*;

public final class ContractQueryResponse {

    private static final String TERMS_URL =
            "https://filer.bucket.nexsol.ai/buckets/tpa-travel-dev/insurance/term.pdf";

    private ContractQueryResponse() {}

    // ── 목록 조회 ──

    public record ContractListItem(
            Long id,
            Long insurerId,
            String insurerName,
            Long partnerId,
            String partnerName,
            Long channelId,
            String channelName,
            Long planId,
            Long familyId,
            String planName,
            String policyNumber,
            String countryName,
            String countryCode,
            Integer insuredPeopleNumber,
            BigDecimal totalPremium,
            String status,
            String authUniqueKey,
            String authStatus,
            LocalDateTime authDate,
            LocalDateTime applyDate,
            LocalDate insureStartDate,
            LocalDate insureEndDate,
            Payment payment,
            List<PersonSummary> people,
            String contractPeopleName,
            String contractPeopleResidentNumberMasked,
            String contractPeopleHp,
            String contractPeopleMail,
            String termsUrl,
            String policyLink) {

        public static ContractListItem of(
                TravelContractEntity c,
                TravelInsurePaymentEntity pay,
                TravelInsurancePlanEntity plan,
                List<TravelInsurePeopleEntity> people) {

            return new ContractListItem(
                    c.getId(),
                    c.getInsurerId(),
                    c.getInsurerName(),
                    c.getPartnerId(),
                    c.getPartnerName(),
                    c.getChannelId(),
                    c.getChannelName(),
                    c.getPlanId(),
                    c.getFamilyId(),
                    plan != null
                            ? toDisplayName(plan.getPlanFullName(), plan.getPlanName())
                            : null,
                    c.getPolicyNumber(),
                    c.getCountryName(),
                    c.getCountryCode(),
                    people.size(),
                    c.getTotalPremium(),
                    c.getStatus() != null ? c.getStatus().name() : null,
                    c.getAuthUniqueKey(),
                    c.getAuthStatus(),
                    c.getAuthDate(),
                    c.getApplyDate(),
                    c.getInsureStartDate(),
                    c.getInsureEndDate(),
                    Payment.of(pay),
                    people.stream()
                            .map(p -> new PersonSummary(p.getId(), p.getName()))
                            .toList(),
                    c.getContractPeopleName(),
                    maskRrn(c.getContractPeopleResidentNumber()),
                    c.getContractPeopleHp(),
                    c.getContractPeopleMail(),
                    TERMS_URL,
                    c.getPolicyLink());
        }
    }

    public record PersonSummary(Long id, String name) {}

    // ── 상세 조회 ──

    public record ContractDetail(
            Contract contract,
            Insurer insurer,
            Partner partner,
            Channel channel,
            Plan plan,
            Payment payment,
            String termsUrl,
            String policyLink,
            List<Person> people) {

        public static ContractDetail of(
                TravelContractEntity c,
                TravelInsurePaymentEntity pay,
                List<TravelInsurePeopleEntity> people,
                TravelInsurancePlanEntity plan,
                TravelInsurerEntity insurer,
                TpaPartnerEntity partner,
                TpaChannelEntity channel) {

            return new ContractDetail(
                    Contract.of(c),
                    Insurer.of(insurer),
                    Partner.of(partner),
                    Channel.of(channel),
                    Plan.of(plan),
                    Payment.of(pay),
                    TERMS_URL,
                    c.getPolicyLink(),
                    people.stream().map(Person::of).toList());
        }
    }

    // ── 공통 inner record ──

    public record Contract(
            Long id,
            Long insurerId,
            Long partnerId,
            Long channelId,
            Long planId,
            Long familyId,
            String insurerName,
            String partnerName,
            String channelName,
            String policyNumber,
            String meritzQuoteGroupNumber,
            String meritzQuoteRequestNumber,
            String countryName,
            String countryCode,
            BigDecimal totalPremium,
            String policyLink,
            String status,
            LocalDateTime applyDate,
            LocalDate insureStartDate,
            LocalDate insureEndDate,
            String contractPeopleName,
            String contractPeopleResidentNumberMasked,
            String contractPeopleHp,
            String contractPeopleMail,
            boolean marketingConsentUsed,
            String authProvider,
            String authImpUid,
            String authRequestId,
            String authUniqueKey,
            String authStatus,
            LocalDateTime authDate,
            Long employeeId) {

        public static Contract of(TravelContractEntity c) {
            return new Contract(
                    c.getId(),
                    c.getInsurerId(),
                    c.getPartnerId(),
                    c.getChannelId(),
                    c.getPlanId(),
                    c.getFamilyId(),
                    c.getInsurerName(),
                    c.getPartnerName(),
                    c.getChannelName(),
                    c.getPolicyNumber(),
                    c.getMeritzQuoteGroupNumber(),
                    c.getMeritzQuoteRequestNumber(),
                    c.getCountryName(),
                    c.getCountryCode(),
                    c.getTotalPremium(),
                    c.getPolicyLink(),
                    c.getStatus() != null ? c.getStatus().name() : null,
                    c.getApplyDate(),
                    c.getInsureStartDate(),
                    c.getInsureEndDate(),
                    c.getContractPeopleName(),
                    maskRrn(c.getContractPeopleResidentNumber()),
                    c.getContractPeopleHp(),
                    c.getContractPeopleMail(),
                    Boolean.TRUE.equals(c.getMarketingConsentUsed()),
                    c.getAuthProvider(),
                    c.getAuthImpUid(),
                    c.getAuthRequestId(),
                    c.getAuthUniqueKey(),
                    c.getAuthStatus(),
                    c.getAuthDate(),
                    c.getEmployeeId());
        }
    }

    public record Payment(
            Long id,
            String paymentMethod,
            String status,
            BigDecimal paidAmount,
            LocalDateTime paymentDate,
            LocalDateTime cancelDate) {

        public static Payment of(TravelInsurePaymentEntity pay) {
            if (pay == null) return null;
            return new Payment(
                    pay.getId(),
                    pay.getPaymentMethod() != null ? pay.getPaymentMethod().name() : null,
                    pay.getStatus() != null ? pay.getStatus().name() : null,
                    pay.getPaidAmount(),
                    pay.getPaymentDate(),
                    pay.getCancelDate());
        }
    }

    public record Person(
            Long id,
            Long planId,
            boolean isContractor,
            String name,
            String nameEng,
            String gender,
            String residentNumberMasked,
            String passportNumberMasked,
            String policyNumber,
            BigDecimal insurePremium) {

        public static Person of(TravelInsurePeopleEntity p) {
            return new Person(
                    p.getId(),
                    p.getPlanId(),
                    Boolean.TRUE.equals(p.getIsContractor()),
                    p.getName(),
                    p.getNameEng(),
                    p.getGender(),
                    maskRrn(p.getResidentNumber()),
                    maskPassport(p.getPassportNumber()),
                    p.getPolicyNumber(),
                    p.getInsurePremium());
        }
    }

    public record Insurer(Long id, String insurerCode, String insurerName) {
        public static Insurer of(TravelInsurerEntity e) {
            if (e == null) return null;
            return new Insurer(e.getId(), e.getInsurerCode(), e.getInsurerName());
        }
    }

    public record Partner(Long id, String partnerCode, String partnerName) {
        public static Partner of(TpaPartnerEntity e) {
            if (e == null) return null;
            return new Partner(e.getId(), e.getPartnerCode(), e.getPartnerName());
        }
    }

    public record Channel(Long id, String channelCode, String channelName) {
        public static Channel of(TpaChannelEntity e) {
            if (e == null) return null;
            return new Channel(e.getId(), e.getChannelCode(), e.getChannelName());
        }
    }

    public record Plan(
            Long id,
            String insuranceProductName,
            String planName,
            String planFullName,
            String productCode,
            String unitProductCode,
            String planGroupCode,
            String planCode) {

        public static Plan of(TravelInsurancePlanEntity e) {
            if (e == null) return null;
            return new Plan(
                    e.getId(),
                    e.getInsuranceProductName(),
                    toDisplayName(e.getPlanFullName(), e.getPlanName()),
                    e.getPlanFullName(),
                    e.getProductCode(),
                    e.getUnitProductCode(),
                    e.getPlanGroupCode(),
                    e.getPlanCode());
        }
    }

    // ── Utilities ──

    private static String toDisplayName(String planFullName, String planName) {
        String name = planFullName != null ? planFullName : planName;
        if (name == null) return null;
        return name.replace("플랜A", "플랜")
                .replace("플랜B", "플랜")
                .replace(" 실손제외", "(실손제외)")
                .replaceAll("_\\d+~\\d+세$", "")
                .trim();
    }

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
package com.nexsol.tpa.storage.db.core.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.nexsol.tpa.core.enums.TravelContractStatus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_contract")
public class TravelContractEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "insurer_id", nullable = false)
    private Long insurerId;

    @Column(name = "insurer_name")
    private String insurerName;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "partner_name")
    private String partnerName;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "family_id")
    private Long familyId;

    @Column(name = "policy_number")
    private String policyNumber;

    @Column(name = "meritz_quote_group_number")
    private String meritzQuoteGroupNumber;

    @Column(name = "meritz_quote_request_number")
    private String meritzQuoteRequestNumber;

    @Column(name = "country_name")
    private String countryName;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "total_premium", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPremium = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TravelContractStatus status = TravelContractStatus.PENDING;

    @Column(name = "apply_date", insertable = false, updatable = false)
    private LocalDateTime applyDate;

    @Column(name = "insure_start_date")
    private LocalDate insureStartDate;

    @Column(name = "insure_end_date")
    private LocalDate insureEndDate;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "policy_link", length = 255)
    private String policyLink;

    @Column(name = "marketing_consent_used", nullable = false)
    private Boolean marketingConsentUsed = false;

    @Column(name = "auth_provider", length = 20)
    private String authProvider;

    @Column(name = "auth_imp_uid", length = 100)
    private String authImpUid;

    @Column(name = "auth_request_id", length = 100)
    private String authRequestId;

    @Column(name = "auth_unique_key", length = 200)
    private String authUniqueKey;

    @Column(name = "auth_status", length = 20)
    private String authStatus = "NONE";

    @Column(name = "auth_date")
    private LocalDateTime authDate;

    // ── 생성 ──

    public static TravelContractEntity create(
            Long insurerId,
            String insurerName,
            Long partnerId,
            String partnerName,
            Long channelId,
            String channelName,
            Long familyId) {

        TravelContractEntity entity = new TravelContractEntity();
        entity.insurerId = insurerId;
        entity.insurerName = insurerName;
        entity.partnerId = partnerId;
        entity.partnerName = partnerName;
        entity.channelId = channelId;
        entity.channelName = channelName;
        entity.familyId = familyId;
        return entity;
    }

    // ── 도메인 업데이트 메서드 ──

    public void applyInsurePeriod(
            LocalDate startDate, LocalDate endDate, String countryCode, String countryName) {
        this.insureStartDate = startDate;
        this.insureEndDate = endDate;
        this.countryCode = countryCode;
        this.countryName = countryName;
    }

    public void applyMeritzQuote(
            String policyNumber, String quoteGroupNumber, String quoteRequestNumber) {
        this.policyNumber = policyNumber;
        this.meritzQuoteGroupNumber = quoteGroupNumber;
        this.meritzQuoteRequestNumber = quoteRequestNumber;
    }

    public void applyPremium(BigDecimal totalPremium) {
        this.totalPremium = totalPremium != null ? totalPremium : BigDecimal.ZERO;
    }

    public void applyMarketingConsent(boolean used) {
        this.marketingConsentUsed = used;
    }

    public void markCompleted() {
        this.status = TravelContractStatus.COMPLETED;
        markUpdated(null);
    }

    public void markError() {
        this.status = TravelContractStatus.ERROR;
        markUpdated(null);
    }

    public void updateMeritzResult(
            BigDecimal ttPrem, String polNo, String quotGrpNo, String quotReqNo) {
        if (ttPrem != null) this.totalPremium = ttPrem;
        if (polNo != null && !polNo.isBlank()) this.policyNumber = polNo;
        if (quotGrpNo != null && !quotGrpNo.isBlank()) this.meritzQuoteGroupNumber = quotGrpNo;
        if (quotReqNo != null && !quotReqNo.isBlank()) this.meritzQuoteRequestNumber = quotReqNo;
    }

    public void updatePolicyLink(String policyLink) {
        if (policyLink != null && !policyLink.isBlank()) this.policyLink = policyLink;
    }

    public void updateAuth(
            String provider, String impUid, String requestId, String uniqueKey, String status) {
        this.authProvider = provider;
        this.authImpUid = impUid;
        this.authRequestId = requestId;
        this.authUniqueKey = uniqueKey;
        this.authStatus = status;
        this.authDate = LocalDateTime.now();
    }
}

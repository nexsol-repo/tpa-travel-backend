package com.nexsol.tpa.storage.db.core.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_insured")
public class TravelInsuredEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "is_contractor", nullable = false)
    private Boolean isContractor;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "resident_number", length = 20)
    private String residentNumber;

    @Column(name = "english_name", length = 100)
    private String englishName;

    @Column(name = "passport_number", length = 30)
    private String passportNumber;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "policy_number", length = 50)
    private String policyNumber;

    @Column(name = "insure_premium", nullable = false, precision = 15, scale = 2)
    private BigDecimal insurePremium;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static TravelInsuredEntity create(
            Long contractId,
            Long planId,
            boolean isContractor,
            String name,
            String gender,
            String residentNumber,
            String englishName,
            String passportNumber,
            String phone,
            String email,
            String policyNumber,
            BigDecimal insurePremium) {
        return new TravelInsuredEntity(
                contractId,
                planId,
                isContractor,
                name,
                gender,
                residentNumber,
                englishName,
                passportNumber,
                phone,
                email,
                policyNumber,
                insurePremium);
    }

    @Builder
    private TravelInsuredEntity(
            Long contractId,
            Long planId,
            Boolean isContractor,
            String name,
            String gender,
            String residentNumber,
            String englishName,
            String passportNumber,
            String phone,
            String email,
            String policyNumber,
            BigDecimal insurePremium) {
        this.contractId = contractId;
        this.planId = planId;
        this.isContractor = isContractor != null && isContractor;
        this.name = name;
        this.gender = gender;
        this.residentNumber = residentNumber;
        this.englishName = englishName;
        this.passportNumber = passportNumber;
        this.phone = phone;
        this.email = email;
        this.policyNumber = policyNumber;
        this.insurePremium = insurePremium == null ? BigDecimal.ZERO : insurePremium;
    }
}

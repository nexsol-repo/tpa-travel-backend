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

    @Column(name = "insure_people_name", length = 50)
    private String name;

    @Column(name = "insure_people_gender", length = 10)
    private String gender;

    @Column(name = "insure_people_resident_number", length = 20)
    private String residentNumber;

    @Column(name = "insure_people_name_eng", length = 100)
    private String nameEng;

    @Column(name = "insure_people_passport_number", length = 30)
    private String passportNumber;

    @Column(name = "policy_number", length = 50)
    private String policyNumber;

    @Column(name = "insure_premium", nullable = false, precision = 15, scale = 2)
    private BigDecimal insurePremium;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public TravelInsurePeopleEntity(
            Long contractId,
            Long planId,
            Boolean isContractor,
            String name,
            String gender,
            String residentNumber,
            String nameEng,
            String passportNumber,
            String policyNumber,
            BigDecimal insurePremium) {
        this.contractId = contractId;
        this.planId = planId;
        this.isContractor = isContractor != null && isContractor;
        this.name = name;
        this.gender = gender;
        this.residentNumber = residentNumber;
        this.nameEng = nameEng;
        this.passportNumber = passportNumber;
        this.policyNumber = policyNumber;
        this.insurePremium = insurePremium == null ? BigDecimal.ZERO : insurePremium;
    }
}

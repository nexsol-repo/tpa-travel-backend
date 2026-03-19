package com.nexsol.tpa.storage.db.core.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_insurance_plan")
public class TravelInsurancePlanEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "insurer_id", nullable = false)
    private Long insurerId;

    @Column(name = "family_id")
    private Long familyId;

    @Column(name = "insurance_product_name")
    private String insuranceProductName;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "product_code")
    private String productCode; // pdCd

    @Column(name = "unit_product_code")
    private String unitProductCode; // untPdCd

    @Column(name = "plan_group_code")
    private String planGroupCode; // planGrpCd

    @Column(name = "plan_code")
    private String planCode; // planCd

    @Column(name = "age_group_id")
    private Integer ageGroupId;

    @Column(name = "plan_full_name")
    private String planFullName;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Builder
    public TravelInsurancePlanEntity(
            Long id,
            Long insurerId,
            Long familyId,
            String insuranceProductName,
            String planName,
            String productCode,
            String unitProductCode,
            String planGroupCode,
            String planCode,
            Integer ageGroupId,
            String planFullName,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Boolean isActive,
            Integer sortOrder) {
        this.id = id;
        this.insurerId = insurerId;
        this.familyId = familyId;
        this.insuranceProductName = insuranceProductName;
        this.planName = planName;
        this.productCode = productCode;
        this.unitProductCode = unitProductCode;
        this.planGroupCode = planGroupCode;
        this.planCode = planCode;
        this.ageGroupId = ageGroupId;
        this.planFullName = planFullName;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.isActive = isActive == null ? true : isActive;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
    }
}

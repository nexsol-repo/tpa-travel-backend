package com.nexsol.tpa.core.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "travel_insurance_plan")
public class TravelInsurancePlanEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "insurer_id", nullable = false)
    private Long insurerId;

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
    private Boolean isActive = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

}

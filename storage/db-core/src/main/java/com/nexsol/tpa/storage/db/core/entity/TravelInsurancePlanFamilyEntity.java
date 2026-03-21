package com.nexsol.tpa.storage.db.core.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.plan.PlanFamily;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "travel_insurance_plan_family",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_plan_family",
                    columnNames = {"insurer_id", "insurance_product_name", "family_name"})
        },
        indexes = {
            @Index(
                    name = "idx_plan_family_lookup",
                    columnList = "insurer_id,insurance_product_name,is_active,sort_order")
        })
public class TravelInsurancePlanFamilyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "insurer_id", nullable = false)
    private Long insurerId;

    @Column(name = "insurance_product_name", nullable = false, length = 100)
    private String insuranceProductName;

    @Column(name = "family_name", nullable = false, length = 100)
    private String familyName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_loss")
    private Boolean isLoss;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public TravelInsurancePlanFamilyEntity(
            Long insurerId,
            String insuranceProductName,
            String familyName,
            Integer sortOrder,
            Boolean isActive,
            Boolean isLoss) {
        this.insurerId = insurerId;
        this.insuranceProductName = insuranceProductName;
        this.familyName = familyName;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
        this.isActive = isActive == null ? true : isActive;
        this.isLoss = isLoss == null ? false : isLoss;
    }

    public PlanFamily toDomain(List<InsurancePlan> plans) {
        return PlanFamily.builder()
                .familyId(id)
                .familyName(familyName)
                .isLoss(Boolean.TRUE.equals(isLoss))
                .plans(plans)
                .build();
    }
}

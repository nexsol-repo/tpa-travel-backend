package com.nexsol.tpa.storage.db.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
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

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;
}

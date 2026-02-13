package com.nexsol.tpa.core.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "travel_insurance_plan_family_map",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_family_plan", columnNames = {"family_id","plan_id"})
        },
        indexes = {
                @Index(name = "idx_map_family", columnList = "family_id"),
                @Index(name = "idx_map_plan", columnList = "plan_id")
        }
)
public class TravelInsurancePlanFamilyMapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private java.time.LocalDateTime createdAt;
}

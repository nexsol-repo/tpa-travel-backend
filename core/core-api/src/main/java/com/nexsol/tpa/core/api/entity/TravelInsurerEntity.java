package com.nexsol.tpa.core.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "travel_insurer",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_insurer_code", columnNames = "insurer_code")
        }
)
public class TravelInsurerEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 보험사 코드 (예: MERITZ) */
    @Column(name = "insurer_code", nullable = false, length = 30)
    private String insurerCode;

    /** 보험사명 (예: 메리츠화재) */
    @Column(name = "insurer_name", nullable = false, length = 100)
    private String insurerName;

    /** 보험사 API Base URL */
    @Column(name = "api_base_url", length = 200)
    private String apiBaseUrl;

    /** 사용 여부 */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}

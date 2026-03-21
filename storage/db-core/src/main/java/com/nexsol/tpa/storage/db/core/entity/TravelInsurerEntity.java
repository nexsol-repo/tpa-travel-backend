package com.nexsol.tpa.storage.db.core.entity;

import java.time.LocalDateTime;

import com.nexsol.tpa.core.domain.plan.Insurer;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "tpa_insurer",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_insurer_code", columnNames = "insurer_code")
        })
public class TravelInsurerEntity extends BaseEntity {

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

    /**
     * 서비스 타입(JSON) 예: {"travel": true, "health": false}
     */
    @Column(name = "service_type", columnDefinition = "longtext")
    private String serviceType;

    /** 사용 여부 */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /** 삭제일시 (soft delete) */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public TravelInsurerEntity(
            String insurerCode,
            String insurerName,
            String apiBaseUrl,
            String serviceType,
            Boolean isActive) {
        this.insurerCode = insurerCode;
        this.insurerName = insurerName;
        this.apiBaseUrl = apiBaseUrl;
        this.serviceType = serviceType;
        this.isActive = isActive == null ? true : isActive;
    }

    public Insurer toDomain() {
        return Insurer.builder().id(id).code(insurerCode).name(insurerName).build();
    }
}

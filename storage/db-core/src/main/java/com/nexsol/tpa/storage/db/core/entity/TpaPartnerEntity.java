package com.nexsol.tpa.storage.db.core.entity;

import java.time.LocalDateTime;

import com.nexsol.tpa.core.domain.contract.Partner;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "tpa_partner",
        indexes = {@Index(name = "idx_partner_name", columnList = "partner_name")},
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_partner_code", columnNames = "partner_code")
        })
public class TpaPartnerEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partner_code", length = 30)
    private String partnerCode;

    @Column(name = "partner_name", nullable = false, length = 100)
    private String partnerName;

    @Column(name = "business_registration_number", length = 20)
    private String businessRegistrationNumber;

    @Column(name = "ceo_name", length = 50)
    private String ceoName;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @Lob
    @Column(name = "service_type", columnDefinition = "longtext")
    private String serviceType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /** soft delete */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public TpaPartnerEntity(
            String partnerCode,
            String partnerName,
            String businessRegistrationNumber,
            String ceoName,
            String address,
            String memo,
            String serviceType,
            Boolean isActive) {
        this.partnerCode = partnerCode;
        this.partnerName = partnerName;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.ceoName = ceoName;
        this.address = address;
        this.memo = memo;
        this.serviceType = serviceType;
        this.isActive = isActive == null ? true : isActive;
    }

    public Partner toDomain() {
        return Partner.builder()
                .id(id)
                .code(partnerCode)
                .name(partnerName)
                .build();
    }
}

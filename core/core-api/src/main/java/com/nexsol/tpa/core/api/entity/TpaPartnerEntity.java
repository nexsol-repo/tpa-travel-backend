package com.nexsol.tpa.core.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tpa_partner", indexes = { @Index(name = "idx_partner_name", columnList = "partner_name") },
        uniqueConstraints = { @UniqueConstraint(name = "uq_partner_code", columnNames = "partner_code") })
public class TpaPartnerEntity extends AuditEntity {

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
    private Boolean isActive = true;

    /** soft delete */
    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;

}

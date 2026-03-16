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
        name = "tpa_channel",
        indexes = {@Index(name = "idx_channel_name", columnList = "channel_name")},
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_channel_code", columnNames = "channel_code")
        })
public class TpaChannelEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK: tpa_partner.id (nullable) */
    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "channel_code", length = 30)
    private String channelCode;

    @Column(name = "channel_name", nullable = false, length = 100)
    private String channelName;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * JSON string (MySQL JSON valid check) ex) ["PUNGSU","TRAVEL","SOLAR"]
     */
    @Lob
    @Column(name = "service_type", columnDefinition = "longtext")
    private String serviceType;

    /** soft delete */
    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;
}

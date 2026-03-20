package com.nexsol.tpa.storage.db.core.entity;

import java.time.LocalDateTime;

import com.nexsol.tpa.core.domain.contract.Channel;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /**
     * JSON string (MySQL JSON valid check) ex) ["PUNGSU","TRAVEL","SOLAR"]
     */
    @Lob
    @Column(name = "service_type", columnDefinition = "longtext")
    private String serviceType;

    /** soft delete */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public TpaChannelEntity(
            Long partnerId,
            String channelCode,
            String channelName,
            Boolean isActive,
            String serviceType) {
        this.partnerId = partnerId;
        this.channelCode = channelCode;
        this.channelName = channelName;
        this.isActive = isActive == null ? true : isActive;
        this.serviceType = serviceType;
    }

    public Channel toDomain() {
        return Channel.builder()
                .id(id)
                .code(channelCode)
                .name(channelName)
                .build();
    }
}

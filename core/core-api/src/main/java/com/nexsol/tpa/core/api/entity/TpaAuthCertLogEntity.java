package com.nexsol.tpa.core.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(
        name = "tpa_auth_cert_log",
        indexes = {
                @Index(name = "idx_auth_log_requestid", columnList = "request_id"),
                @Index(name = "idx_auth_log_insured", columnList = "insured_seq_no"),
                @Index(name = "idx_auth_log_created", columnList = "created_at"),
                @Index(name = "idx_auth_log_biznum", columnList = "biz_num")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_auth_log_impuid", columnNames = "imp_uid")
        }
)
public class TpaAuthCertLogEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** bizNum(현재는 insuredBirthDay 등으로 사용중) */
    @Column(name = "biz_num", nullable = false, length = 20)
    private String bizNum;

    /** 아임포트 imp_uid */
    @Column(name = "imp_uid", nullable = false, length = 100)
    private String impUid;

    /** 다날/아임포트 request_id */
    @Column(name = "request_id", length = 100)
    private String requestId;

    /** 유입경로/상품 식별 (ex: dsf6, ti) */
    @Column(name = "path_root", nullable = false, length = 50)
    private String pathRoot;

    /** merchant_uid(=athNo/ORD_...) */
    @Column(name = "moid", length = 100)
    private String moid;

    /** pg(inicis_unified.CPID 등) */
    @Column(name = "pg", length = 100)
    private String pg;

    /** 인증사업자 */
    @Column(name = "provider", nullable = false, length = 20)
    private String provider = "DANAL_PASS";

    /** User-Agent */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /** 클라이언트 IP */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    /** 페이지 URL/Referer */
    @Column(name = "referer", length = 500)
    private String referer;

    /**
     * 요청 원문(JSON)
     * - MySQL JSON 컬럼
     * - JPA에선 일단 String으로 저장 (필요하면 Converter/JsonType로 고도화)
     */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "raw_req_json", columnDefinition = "longtext")
    private String rawReqJson;

}

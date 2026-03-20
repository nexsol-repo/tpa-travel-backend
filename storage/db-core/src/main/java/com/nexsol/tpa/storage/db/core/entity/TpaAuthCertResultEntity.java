package com.nexsol.tpa.storage.db.core.entity;

import com.nexsol.tpa.core.domain.auth.AuthCertResultInfo;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "tpa_auth_cert_result",
        indexes = {
            @Index(name = "idx_auth_result_insured", columnList = "insured_seq_no"),
            @Index(name = "idx_auth_result_status", columnList = "result_status"),
            @Index(name = "idx_auth_result_created", columnList = "created_at"),
            @Index(name = "idx_auth_result_logid", columnList = "log_id")
        },
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_auth_result_impuid", columnNames = "imp_uid")
        })
public class TpaAuthCertResultEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * tpa_auth_cert_log.id (없으면 NULL) - FK: ON DELETE SET NULL
     */
    @Column(name = "log_id")
    private Long logId;

    /** 아임포트 imp_uid */
    @Column(name = "imp_uid", nullable = false, length = 100)
    private String impUid;

    /** request_id */
    @Column(name = "request_id", length = 100)
    private String requestId;

    /** merchant_uid(=athNo/ORD_...) */
    @Column(name = "moid", length = 100)
    private String moid;

    /** 서버 발급 unique_key(insuredUniqueKey) */
    @Column(name = "unique_key", length = 200)
    private String uniqueKey;

    /** SUCCESS/FAIL */
    @Column(name = "result_status", nullable = false, length = 20)
    private String resultStatus;

    /** 결과코드(내부/외부) */
    @Column(name = "result_code", length = 50)
    private String resultCode;

    /** 결과메시지 */
    @Column(name = "result_msg", length = 1000)
    private String resultMsg;

    /** 인증된 이름 */
    @Column(name = "cert_name", length = 100)
    private String certName;

    /** 인증된 생년월일(YYYYMMDD) */
    @Column(name = "cert_birthday", length = 8)
    private String certBirthday;

    /** 성별 */
    @Column(name = "cert_gender", length = 10)
    private String certGender;

    /** 휴대폰(옵션) */
    @Column(name = "cert_phone", length = 20)
    private String certPhone;

    /** 대표가입자 정보와 매칭 여부(Y/N) */
    @Column(name = "matched_yn", nullable = false, length = 1)
    private String matchedYn;

    /** 불일치 사유(옵션) */
    @Column(name = "match_fail_reason", length = 200)
    private String matchFailReason;

    /** 응답 원문(JSON) */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "raw_res_json", columnDefinition = "json")
    private String rawResJson;

    public static TpaAuthCertResultEntity createEmpty() {
        return new TpaAuthCertResultEntity();
    }

    @Builder
    public TpaAuthCertResultEntity(
            Long logId,
            String impUid,
            String requestId,
            String moid,
            String uniqueKey,
            String resultStatus,
            String resultCode,
            String resultMsg,
            String certName,
            String certBirthday,
            String certGender,
            String certPhone,
            String matchedYn,
            String matchFailReason,
            String rawResJson) {
        this.logId = logId;
        this.impUid = impUid;
        this.requestId = requestId;
        this.moid = moid;
        this.uniqueKey = uniqueKey;
        this.resultStatus = resultStatus;
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.certName = certName;
        this.certBirthday = certBirthday;
        this.certGender = certGender;
        this.certPhone = certPhone;
        this.matchedYn = matchedYn == null ? "N" : matchedYn;
        this.matchFailReason = matchFailReason;
        this.rawResJson = rawResJson;
    }

    public void update(
            Long logId,
            String impUid,
            String requestId,
            String moid,
            String uniqueKey,
            String resultStatus,
            String resultCode,
            String resultMsg,
            String certName,
            String certBirthday,
            String certGender,
            String certPhone,
            String matchedYn,
            String matchFailReason,
            String rawResJson) {
        this.logId = logId;
        this.impUid = impUid;
        this.requestId = requestId;
        this.moid = moid;
        this.uniqueKey = uniqueKey;
        this.resultStatus = resultStatus;
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.certName = certName;
        this.certBirthday = certBirthday;
        this.certGender = certGender;
        this.certPhone = certPhone;
        this.matchedYn = matchedYn;
        this.matchFailReason = matchFailReason;
        this.rawResJson = rawResJson;
    }

    public AuthCertResultInfo toDomain() {
        return AuthCertResultInfo.builder()
                .id(id)
                .logId(logId)
                .moid(moid)
                .impUid(impUid)
                .requestId(requestId)
                .uniqueKey(uniqueKey)
                .resultStatus(resultStatus)
                .resultCode(resultCode)
                .resultMsg(resultMsg)
                .certName(certName)
                .certBirthday(certBirthday)
                .certGender(certGender)
                .certPhone(certPhone)
                .matched("Y".equals(matchedYn))
                .matchedYn(matchedYn)
                .matchFailReason(matchFailReason)
                .rawResJson(rawResJson)
                .build();
    }

    public static TpaAuthCertResultEntity fromDomain(AuthCertResultInfo info) {
        return TpaAuthCertResultEntity.builder()
                .logId(info.logId())
                .moid(info.moid())
                .impUid(info.impUid())
                .requestId(info.requestId())
                .uniqueKey(info.uniqueKey())
                .resultStatus(info.resultStatus())
                .resultCode(info.resultCode())
                .resultMsg(info.resultMsg())
                .certName(info.certName())
                .certBirthday(info.certBirthday())
                .certGender(info.certGender())
                .certPhone(info.certPhone())
                .matchedYn(info.matchedYn())
                .matchFailReason(info.matchFailReason())
                .rawResJson(info.rawResJson())
                .build();
    }
}

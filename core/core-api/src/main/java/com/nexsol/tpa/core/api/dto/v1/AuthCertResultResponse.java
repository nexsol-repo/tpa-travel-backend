package com.nexsol.tpa.core.api.dto.v1;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthCertResultResponse {

    /** 인증 트랜잭션 식별 */
    private String moid; // merchant_uid (ORD_...)

    private String impUid; // 아임포트 imp_uid

    private String requestId; // request_id

    private Long insuredSeqNo; // 가입/피보험자 seq (기본 0)

    /** 서버 발급 고유키 */
    private String uniqueKey; // insuredUniqueKey

    /** 결과 상태 */
    private String resultStatus; // SUCCESS / FAIL

    private String resultCode; // 결과 코드

    private String resultMsg; // 결과 메시지

    /** 인증된 사용자 정보 */
    private String certName; // 인증된 이름

    private String certBirthday; // 생년월일 (YYYYMMDD)

    private String certGender; // 성별(M/F or 1/2)

    private String certPhone; // 휴대폰 번호

    /** 계약자 정보와 매칭 여부 */
    private String matchedYn; // Y / N

    private String matchFailReason; // 불일치 사유 (옵션)

}

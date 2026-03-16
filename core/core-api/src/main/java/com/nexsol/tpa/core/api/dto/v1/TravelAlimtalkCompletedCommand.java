package com.nexsol.tpa.core.api.dto.v1;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TravelAlimtalkCompletedCommand {

    private String receiverHp; // 010...

    private String receiverName; // 홍길동

    private String productName; // 여행자보험

    private String policyNumber; // 증권번호

    private String certificateUrl; // 가입확인서 링크

    private String termsUrl; // 약관 링크
}

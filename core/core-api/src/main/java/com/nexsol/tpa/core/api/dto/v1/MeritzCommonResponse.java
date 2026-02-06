package com.nexsol.tpa.core.api.dto.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeritzCommonResponse {

    // 공통
    private String errCd;

    private String errMsg;

    // estSave / prem 계열에서 종종 내려오는 값들
    private BigDecimal ttPrem; // 총보험료(문자열로 올 수도 있음 -> 아래 참고)

    private String polNo; // 증권번호

    private String quotGrpNo; // 견적그룹번호

    private String quotReqNo; // 견적요청번호

}

package com.nexsol.tpa.core.api.meritz.dto.quote;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MeritzHndyPremCmptBody(
        // 필수
        String gnrAflcoCd, String aflcoDivCd, String bizpeNo, String polNo, String pdCd, String untPdCd, String sbcpDt,
        String insBgnDt, String insEdDt, String trvArCd, String inspeCnt, List<Insured> opapiTrvPremCmptInspeInfCbcVo) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Insured(
            // 명세: inspeBdt 필수, gndrCd 필수, 나머지 선택/필수 혼합
            String inspeBdt, String gndrCd, String inspeRsidNo, String inspeNm, String engInspeNm, String planGrpCd,
            String planCd) {
    }
}

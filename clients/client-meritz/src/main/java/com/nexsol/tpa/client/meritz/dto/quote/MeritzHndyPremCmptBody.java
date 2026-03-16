package com.nexsol.tpa.client.meritz.dto.quote;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MeritzHndyPremCmptBody(
        String company,
        String gnrAflcoCd,
        String aflcoDivCd,
        String bizpeNo,
        String polNo,
        String pdCd,
        String untPdCd,
        String sbcpDt,
        String insBgnDt,
        String insEdDt,
        String trvArCd,
        int inspeCnt,
        List<Insured> opapiTrvPremCmptInspeInfCbcVo) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Insured(
            String planGrpCd,
            String planCd,
            String inspeBdt,
            String gndrCd,
            String inspeNm,
            String engInspeNm) {}
}

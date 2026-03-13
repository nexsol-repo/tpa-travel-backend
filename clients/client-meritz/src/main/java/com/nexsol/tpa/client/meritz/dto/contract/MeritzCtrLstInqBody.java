package com.nexsol.tpa.client.meritz.dto.contract;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MeritzCtrLstInqBody(Header header, Body body) {
    public record Header(String apiTrxId, String apiReqTm) {}

    public record Body(
            String gnrAflcoCd,
            String aflcoDivCd,
            String bizpeNo,
            String polNo,
            String quotReqNo,
            String ctrStDt,
            String ctrEdDt) {}
}

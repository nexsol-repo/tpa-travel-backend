package com.nexsol.tpa.core.api.meritz.dto.contract;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MeritzJoinCertBody(Header header, Body body) {
    public record Header(String apiTrxId, String apiReqTm) {
    }

    public record Body(String gnrAflcoCd, String aflcoDivCd, String bizpeNo, String polNo, String ctrNo,
            String certLangCd, // KOR / ENG
            String certTypeCd // PDF / HTML
    ) {
    }
}

package com.nexsol.tpa.core.api.meritz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MeritzApiRequest<T>(Header header, T body) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Header(String apiTrxId, String apiReqTm) {
    }
}

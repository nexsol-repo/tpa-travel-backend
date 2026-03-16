package com.nexsol.tpa.core.api.controller.v1.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexsol.tpa.core.api.dto.v1.MeritzCityItemDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MeritzCityResponse {

    private String errCd;

    private String inqCnt;

    @JsonProperty("opapiTrvCityNatlInfCbcVo")
    private List<MeritzCityItemDto> cities;
}

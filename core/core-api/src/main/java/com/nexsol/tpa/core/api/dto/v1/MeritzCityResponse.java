package com.nexsol.tpa.core.api.dto.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MeritzCityResponse {

    private String errCd;

    private String inqCnt;

    @JsonProperty("opapiTrvCityNatlInfCbcVo")
    private List<MeritzCityItemDto> cities;

}

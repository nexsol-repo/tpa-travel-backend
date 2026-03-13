package com.nexsol.tpa.core.api.controller.v1.response;

import com.nexsol.tpa.core.domain.city.CityInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CityResponse {

    private String cityNatlCd;

    private String korNatlNm;

    private String engNatlNm;

    private String korCityNm;

    private String engCityNm;

    private String trvRskGrdeCd;

    private Integer sortOrder;

    public static CityResponse of(CityInfo info) {
        return CityResponse.builder()
                .cityNatlCd(info.cityNatlCd())
                .korNatlNm(info.korNatlNm())
                .engNatlNm(info.engNatlNm())
                .korCityNm(info.korCityNm())
                .engCityNm(info.engCityNm())
                .trvRskGrdeCd(info.trvRskGrdeCd())
                .sortOrder(info.sortOrder())
                .build();
    }
}

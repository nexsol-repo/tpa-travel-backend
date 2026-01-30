package com.nexsol.tpa.core.api.dto;

import com.nexsol.tpa.core.api.entity.FavoriteCityEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CityResponseDto {

    private String cityNatlCd;

    private String korNatlNm;

    private String engNatlNm;

    private String korCityNm;

    private String engCityNm;

    private String trvRskGrdeCd;

    private Integer sortOrder;

    public static CityResponseDto from(FavoriteCityEntity e) {
        return new CityResponseDto(
                e.getCountryCode(),
                e.getCountryNameKorean(),
                e.getCountryNameEnglish(),
                e.getCityNameKorean(),
                e.getCityNameEnglish(),
                e.getTravelRiskGradeCode(),
                e.getSortOrder());
    }

}

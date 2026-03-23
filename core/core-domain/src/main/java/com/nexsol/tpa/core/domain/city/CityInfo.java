package com.nexsol.tpa.core.domain.city;

public record CityInfo(
        String cityNatlCd,
        String korNatlNm,
        String engNatlNm,
        String korCityNm,
        String engCityNm,
        String trvRskGrdeCd,
        Integer sortOrder) {}

package com.nexsol.tpa.core.domain.city;

import lombok.Builder;

@Builder
public record FavoriteCity(
        Long id,
        String countryCode,
        String countryNameKorean,
        String countryNameEnglish,
        String cityNameKorean,
        String cityNameEnglish,
        String travelRiskGradeCode,
        Integer sortOrder) {}
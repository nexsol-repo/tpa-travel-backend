package com.nexsol.tpa.core.domain.city;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.repository.FavoriteCityRepository;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteCityService {

    private final FavoriteCityRepository favoriteCityRepository;

    /**
     * 자주가는 도시 목록 조회 - deleted_at IS NULL - sort_order 오름차순
     */
    public List<CityInfo> getFavoriteCities() {
        return favoriteCityRepository.findAllActive().stream()
                .map(
                        e ->
                                new CityInfo(
                                        e.countryCode(),
                                        e.countryNameKorean(),
                                        e.countryNameEnglish(),
                                        e.cityNameKorean(),
                                        e.cityNameEnglish(),
                                        e.travelRiskGradeCode(),
                                        e.sortOrder()))
                .toList();
    }

    /**
     * 단건 조회 (삭제 제외)
     */
    public FavoriteCity getFavoriteCity(Long id) {
        return favoriteCityRepository
                .findActiveById(id)
                .orElseThrow(
                        () ->
                                new CoreException(
                                        CoreErrorType.NOT_FOUND_DATA,
                                        "자주가는 도시 정보가 없습니다. id=" + id));
    }
}

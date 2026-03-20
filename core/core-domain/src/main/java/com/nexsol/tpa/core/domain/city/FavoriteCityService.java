package com.nexsol.tpa.core.domain.city;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexsol.tpa.storage.db.core.entity.FavoriteCityEntity;
import com.nexsol.tpa.storage.db.core.repository.FavoriteCityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteCityService {

    private final FavoriteCityRepository favoriteCityRepository;

    /**
     * 자주가는 도시 목록 조회 - deleted_at IS NULL - sort_order 오름차순
     */
    public List<CityInfo> getFavoriteCities() {
        return favoriteCityRepository.findByDeletedAtIsNullOrderBySortOrderAsc().stream()
                .map(
                        e ->
                                new CityInfo(
                                        e.getCountryCode(),
                                        e.getCountryNameKorean(),
                                        e.getCountryNameEnglish(),
                                        e.getCityNameKorean(),
                                        e.getCityNameEnglish(),
                                        e.getTravelRiskGradeCode(),
                                        e.getSortOrder()))
                .toList();
    }

    /**
     * 단건 조회 (삭제 제외)
     */
    public FavoriteCityEntity getFavoriteCity(Long id) {
        return favoriteCityRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("자주가는 도시 정보가 없습니다. id=" + id));
    }

    /**
     * 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteFavoriteCity(Long id, String actor) {
        FavoriteCityEntity city =
                favoriteCityRepository
                        .findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(
                                () -> new IllegalArgumentException("자주가는 도시 정보가 없습니다. id=" + id));

        city.softDelete(actor);
        // save() 호출 안 해도 @Transactional + dirty checking으로 반영됨
    }

    /**
     * 정렬 순서 변경
     */
    @Transactional
    public void updateSortOrder(Long id, int sortOrder, String actor) {
        FavoriteCityEntity city =
                favoriteCityRepository
                        .findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(
                                () -> new IllegalArgumentException("자주가는 도시 정보가 없습니다. id=" + id));

        city.changeSortOrder(sortOrder, actor);
    }
}

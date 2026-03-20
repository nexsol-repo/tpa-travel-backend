package com.nexsol.tpa.storage.db.core.entity;

import java.time.LocalDateTime;

import com.nexsol.tpa.core.domain.city.FavoriteCity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "travel_favorite_city")
public class FavoriteCityEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country_code", length = 10, nullable = false)
    private String countryCode;

    @Column(name = "country_name_korean", length = 50, nullable = false)
    private String countryNameKorean;

    @Column(name = "country_name_english", length = 50, nullable = false)
    private String countryNameEnglish;

    @Column(name = "city_name_korean", length = 50, nullable = false)
    private String cityNameKorean;

    @Column(name = "city_name_english", length = 50, nullable = false)
    private String cityNameEnglish;

    @Column(name = "travel_risk_grade_code", length = 10, nullable = false)
    private String travelRiskGradeCode;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public FavoriteCityEntity(
            String countryCode,
            String countryNameKorean,
            String countryNameEnglish,
            String cityNameKorean,
            String cityNameEnglish,
            String travelRiskGradeCode,
            Integer sortOrder) {
        this.countryCode = countryCode;
        this.countryNameKorean = countryNameKorean;
        this.countryNameEnglish = countryNameEnglish;
        this.cityNameKorean = cityNameKorean;
        this.cityNameEnglish = cityNameEnglish;
        this.travelRiskGradeCode = travelRiskGradeCode;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
    }

    public void changeSortOrder(int sortOrder, String actor) {
        this.sortOrder = sortOrder;
        markUpdated(actor);
    }

    public void softDelete(String actor) {
        this.updatedAt = LocalDateTime.now();
    }

    public FavoriteCity toDomain() {
        return FavoriteCity.builder()
                .id(id)
                .countryCode(countryCode)
                .countryNameKorean(countryNameKorean)
                .countryNameEnglish(countryNameEnglish)
                .cityNameKorean(cityNameKorean)
                .cityNameEnglish(cityNameEnglish)
                .travelRiskGradeCode(travelRiskGradeCode)
                .sortOrder(sortOrder)
                .build();
    }

    public static FavoriteCityEntity fromDomain(FavoriteCity c) {
        return FavoriteCityEntity.builder()
                .countryCode(c.countryCode())
                .countryNameKorean(c.countryNameKorean())
                .countryNameEnglish(c.countryNameEnglish())
                .cityNameKorean(c.cityNameKorean())
                .cityNameEnglish(c.cityNameEnglish())
                .travelRiskGradeCode(c.travelRiskGradeCode())
                .sortOrder(c.sortOrder())
                .build();
    }
}

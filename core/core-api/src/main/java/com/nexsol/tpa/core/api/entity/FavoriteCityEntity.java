package com.nexsol.tpa.core.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "travel_favorite_city")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FavoriteCityEntity extends AuditEntity {

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

    @PrePersist
    private void prePersist() {
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    public void changeSortOrder(int sortOrder, String actor) {
        this.sortOrder = sortOrder;
        markUpdated(actor);
    }

    public void softDelete(String actor) {
        markDeleted(actor);
    }
}

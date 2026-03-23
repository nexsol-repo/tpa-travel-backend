package com.nexsol.tpa.storage.db.core.entity;

import java.time.LocalDateTime;

import com.nexsol.tpa.core.domain.coverage.PlanCoverage;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "travel_plan_coverage",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_plan_coverage",
                    columnNames = {"plan_id", "coverage_id"})
        },
        indexes = {
            @Index(name = "idx_pc_plan", columnList = "plan_id"),
            @Index(name = "idx_pc_coverage", columnList = "coverage_id")
        })
public class TravelPlanCoverageEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 플랜 ID (travel_insurance_plan.id) */
    @Column(name = "plan_id", nullable = false)
    private Long planId;

    /** 담보 ID (travel_insurance_coverage.id) */
    @Column(name = "coverage_id", nullable = false)
    private Long coverageId;

    /** 포함 여부 (0: 미포함, 1: 포함) */
    @Column(name = "is_included", nullable = false)
    private Boolean isIncluded;

    /** 플랜별 노출 담보명 (null이면 coverage_name 사용) */
    @Column(name = "display_name", length = 255)
    private String displayName;

    /** 정렬 순서 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /** 주요 보장 여부 */
    @Column(name = "is_major_coverage", nullable = false)
    private Boolean isMajorCoverage;

    /** 보장금액 타이틀 여부 (0:N,1:Y) */
    @Column(name = "title_yn", nullable = false)
    private Boolean titleYn;

    /** 담보 UI 카테고리 코드 */
    @Column(name = "category_code", length = 50)
    private String categoryCode;

    /** 지급사유 override */
    @Column(name = "claim_reason_override", length = 300)
    private String claimReasonOverride;

    /** 지급내용 override */
    @Column(name = "claim_content_override", columnDefinition = "text")
    private String claimContentOverride;

    /** 추가 제목 override */
    @Column(name = "sub_title_override", length = 200)
    private String subTitleOverride;

    /** 추가 내용 override */
    @Column(name = "sub_content_override", columnDefinition = "text")
    private String subContentOverride;

    /** 삭제일시 (soft delete) */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public TravelPlanCoverageEntity(
            Long planId,
            Long coverageId,
            Boolean isIncluded,
            String displayName,
            Integer sortOrder,
            Boolean isMajorCoverage,
            Boolean titleYn,
            String categoryCode,
            String claimReasonOverride,
            String claimContentOverride,
            String subTitleOverride,
            String subContentOverride) {
        this.planId = planId;
        this.coverageId = coverageId;
        this.isIncluded = isIncluded == null ? true : isIncluded;
        this.displayName = displayName;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
        this.isMajorCoverage = isMajorCoverage == null ? false : isMajorCoverage;
        this.titleYn = titleYn == null ? false : titleYn;
        this.categoryCode = categoryCode;
        this.claimReasonOverride = claimReasonOverride;
        this.claimContentOverride = claimContentOverride;
        this.subTitleOverride = subTitleOverride;
        this.subContentOverride = subContentOverride;
    }

    public PlanCoverage toDomain(String coverageCode, String coverageName) {
        return PlanCoverage.builder()
                .planId(planId)
                .coverageCode(coverageCode)
                .coverageName(coverageName)
                .displayName(displayName)
                .included(Boolean.TRUE.equals(isIncluded))
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .majorCoverage(Boolean.TRUE.equals(isMajorCoverage))
                .titleYn(Boolean.TRUE.equals(titleYn))
                .categoryCode(categoryCode)
                .claimReasonOverride(claimReasonOverride)
                .claimContentOverride(claimContentOverride)
                .subTitleOverride(subTitleOverride)
                .subContentOverride(subContentOverride)
                .build();
    }
}

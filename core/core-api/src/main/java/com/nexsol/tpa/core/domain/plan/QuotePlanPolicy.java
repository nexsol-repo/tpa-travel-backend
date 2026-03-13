package com.nexsol.tpa.core.domain.plan;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.plan.TravelPlanReader.PlanFamily;
import com.nexsol.tpa.core.domain.premium.PlanCondition;
import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;

/**
 * 견적 플랜 선택 정책 도구 (Tool Layer).
 * planType 결정, 패밀리 필터링, 연령대 매핑, 실손제외 매핑 등의 규칙을 담당한다.
 */
@Component
public class QuotePlanPolicy {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 피보험자 나이 기반 planType 결정.
     * 70세 이상이 1명이라도 있으면 "A", 아니면 "B".
     */
    public String resolvePlanType(List<PlanCondition.Insured> insuredList, String insBgnDt) {
        for (PlanCondition.Insured insured : insuredList) {
            int age = calcAge(insured.birth(), insBgnDt);
            if (age >= 70) return "A";
        }
        return "B";
    }

    /**
     * 일반 견적용 패밀리 필터링.
     * planType(A/B)에 해당하고, 실손제외 패밀리는 제외한다.
     */
    public List<PlanFamily> filterFamilies(List<PlanFamily> allFamilies, String planType) {
        String typeMarker = "플랜" + planType;
        List<PlanFamily> result = new ArrayList<>();
        for (PlanFamily f : allFamilies) {
            if (f.familyName() == null) continue;
            if (!f.familyName().contains(typeMarker)) continue;
            if (f.familyName().contains("실손제외")) continue;
            result.add(f);
        }
        return result;
    }

    /**
     * 선택된 planId의 패밀리 → 대응하는 실손제외 패밀리를 찾는다.
     * familyName 기반 매칭: "가뿐한플랜A" → "가뿐한플랜A 실손제외"
     */
    public PlanFamily resolveSilsonExcludeFamily(List<PlanFamily> allFamilies, Long planId) {
        // 1. planId가 속한 원본 패밀리 찾기
        PlanFamily sourceFamily = findFamilyByPlanId(allFamilies, planId);

        // 2. 대응하는 실손제외 패밀리 찾기
        String targetName = sourceFamily.familyName() + " 실손제외";
        return allFamilies.stream()
                .filter(f -> targetName.equals(f.familyName()))
                .findFirst()
                .orElseThrow(
                        () ->
                                new CoreApiException(
                                        CoreApiErrorType.QUOTE_PLAN_NOT_FOUND,
                                        "실손제외 패밀리 없음. source=" + sourceFamily.familyName()));
    }

    private PlanFamily findFamilyByPlanId(List<PlanFamily> allFamilies, Long planId) {
        for (PlanFamily f : allFamilies) {
            for (TravelInsurancePlanEntity p : f.plans()) {
                if (Objects.equals(p.getId(), planId)) {
                    return f;
                }
            }
        }
        throw new CoreApiException(CoreApiErrorType.QUOTE_PLAN_NOT_FOUND, "planId=" + planId);
    }

    /**
     * 나이 → ageGroupId 매핑.
     */
    public Integer resolveAgeGroupId(int age) {
        AgeBand band = AgeBand.fromAge(age);
        return band != null ? band.ageGroupId() : null;
    }

    /**
     * 생년월일 + 기준일 → AgeBand 결정.
     * 나이 계산과 연령대 판정을 일괄 수행한다.
     */
    public AgeBand resolveAgeBand(String birthYmd, String stdDtYmd) {
        return AgeBand.fromAge(calcAge(birthYmd, stdDtYmd));
    }

    /**
     * 나이 계산.
     */
    public int calcAge(String birthYmd, String stdDtYmd) {
        LocalDate birth = LocalDate.parse(birthYmd, YYYYMMDD);
        LocalDate std = LocalDate.parse(stdDtYmd, YYYYMMDD);
        return java.time.Period.between(birth, std).getYears();
    }

    /**
     * 패밀리의 플랜 중 ageGroupId에 해당하는 플랜을 찾는다.
     */
    public TravelInsurancePlanEntity findPlanForAge(
            List<TravelInsurancePlanEntity> familyPlans, int ageGroupId) {
        return familyPlans.stream()
                .filter(p -> Objects.equals(p.getAgeGroupId(), ageGroupId))
                .findFirst()
                .orElse(null);
    }
}

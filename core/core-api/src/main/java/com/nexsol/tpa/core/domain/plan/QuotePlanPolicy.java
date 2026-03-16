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
     * 견적용 패밀리 필터링.
     * planType(A/B)에 해당하고, silsonExclude 여부에 따라 실손포함/제외 패밀리를 반환한다.
     */
    public List<PlanFamily> filterFamilies(
            List<PlanFamily> allFamilies, String planType, boolean silsonExclude) {
        String typeMarker = "플랜" + planType;
        List<PlanFamily> result = new ArrayList<>();
        for (PlanFamily f : allFamilies) {
            if (f.familyName() == null) continue;
            if (!f.familyName().contains(typeMarker)) continue;
            if (silsonExclude && f.isLoss()) continue;
            if (!silsonExclude && !f.isLoss()) continue;
            result.add(f);
        }
        return result;
    }

    /**
     * 선택된 planId의 패밀리 → 대응하는 실손제외 패밀리를 찾는다.
     * isLoss 플래그 기반: 같은 planType의 실손제외(isLoss=false) 패밀리를 찾는다.
     */
    public PlanFamily resolveSilsonExcludeFamily(
            List<PlanFamily> allFamilies, Long planId, String planType) {
        PlanFamily sourceFamily = findFamilyByPlanId(allFamilies, planId);
        String typeMarker = "플랜" + planType;

        return allFamilies.stream()
                .filter(f -> f.familyName() != null)
                .filter(f -> f.familyName().contains(typeMarker))
                .filter(f -> !f.isLoss())
                .filter(f -> matchesFamilyBase(sourceFamily.familyName(), f.familyName()))
                .findFirst()
                .orElseThrow(
                        () ->
                                new CoreApiException(
                                        CoreApiErrorType.QUOTE_PLAN_NOT_FOUND,
                                        "실손제외 패밀리 없음. source=" + sourceFamily.familyName()));
    }

    /**
     * 패밀리 이름의 기본 부분이 일치하는지 확인한다.
     * 예: "가뿐한플랜A"와 "가뿐한플랜A 실손제외"는 같은 기본 패밀리.
     */
    private boolean matchesFamilyBase(String sourceName, String targetName) {
        if (sourceName == null || targetName == null) return false;
        String sourceBase = sourceName.replace(" 실손제외", "").trim();
        String targetBase = targetName.replace(" 실손제외", "").trim();
        return sourceBase.equals(targetBase);
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

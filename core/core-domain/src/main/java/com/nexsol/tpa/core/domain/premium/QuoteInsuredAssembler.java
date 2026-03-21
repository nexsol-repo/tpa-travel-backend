package com.nexsol.tpa.core.domain.premium;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.client.InsuranceQuoteClient.PremiumCommand;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.plan.QuotePlanPolicy;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 보험료 견적 요청 조립 도구 (Tool Layer).
 * 나이→플랜 매핑, 피보험자 조립, API 요청 구성을 담당한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteInsuredAssembler {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String DEFAULT_COMPANY = "TPA";

    /** 견적 단계에서는 실제 이름이 불필요하므로 더미 값을 사용한다. */
    private static final String PLACEHOLDER_NAME = "홍길동";

    private static final String PLACEHOLDER_NAME_ENG = "HONG GIL DONG";

    private final QuotePlanPolicy policy;

    /**
     * 패밀리 플랜 목록과 조건을 기반으로 PremiumCommand를 조립한다.
     * 가족 중 해당 ageGroup의 플랜이 없으면 null을 반환한다.
     * PremiumCommand 는 외부 연동사 이므로 레이어에 포함안됨 개념에도 포함안됨 횡단관심사 영역임
     */
    public PremiumCommand assemble(PlanCondition cmd, List<InsurancePlan> familyPlans) {
        validateAges(cmd);

        Map<Integer, InsurancePlan> planByAgeGroup = indexByAgeGroup(familyPlans);
        InsurancePlan repPlan = selectRepresentativePlan(familyPlans);

        List<PremiumCommand.InsuredPersonCommand> insuredPersons =
                cmd.insuredList().stream()
                        .map(
                                insured ->
                                        resolveInsuredPerson(
                                                insured, cmd.insBgnDt(), planByAgeGroup))
                        .toList();

        if (insuredPersons.contains(null)) {
            log.debug(
                    "[PREMIUM] family has no plan for some ageGroup, repPlanCd={} → skip",
                    repPlan.planCode());
            return null;
        }

        return new PremiumCommand(
                DEFAULT_COMPANY,
                repPlan.productCode(),
                repPlan.unitProductCode(),
                LocalDate.now().format(YYYYMMDD),
                cmd.insBgnDt(),
                cmd.insEdDt(),
                cmd.trvArCd(),
                insuredPersons);
    }

    // ── internal ──

    private PremiumCommand.InsuredPersonCommand resolveInsuredPerson(
            PlanCondition.Insured insured,
            String insBgnDt,
            Map<Integer, InsurancePlan> planByAgeGroup) {

        int ageGroupId = policy.resolveAgeGroupId(policy.calcAge(insured.birth(), insBgnDt));
        InsurancePlan plan = planByAgeGroup.get(ageGroupId);
        if (plan == null) {
            return null;
        }

        return new PremiumCommand.InsuredPersonCommand(
                plan.planGroupCode(),
                plan.planCode(),
                insured.birth(),
                insured.gender(),
                PLACEHOLDER_NAME,
                PLACEHOLDER_NAME_ENG);
    }

    private void validateAges(PlanCondition cmd) {
        List<PlanCondition.Insured> insuredList = cmd.insuredList();
        IntStream.range(0, insuredList.size())
                .forEach(
                        i -> {
                            int age = policy.calcAge(insuredList.get(i).birth(), cmd.insBgnDt());
                            if (policy.resolveAgeGroupId(age) == null) {
                                throw new CoreException(
                                        CoreErrorType.INVALID_QUOTE_REQUEST,
                                        "unsupported age. index=" + i + ", age=" + age);
                            }
                        });
    }

    private Map<Integer, InsurancePlan> indexByAgeGroup(List<InsurancePlan> familyPlans) {
        Map<Integer, InsurancePlan> map = new HashMap<>();
        for (InsurancePlan p : familyPlans) {
            if (p.ageGroupId() != null) {
                map.put(p.ageGroupId(), p);
            }
        }
        return map;
    }

    private InsurancePlan selectRepresentativePlan(List<InsurancePlan> familyPlans) {
        return familyPlans.stream()
                .filter(p -> Objects.equals(p.ageGroupId(), 2))
                .findFirst()
                .orElse(familyPlans.getFirst());
    }
}

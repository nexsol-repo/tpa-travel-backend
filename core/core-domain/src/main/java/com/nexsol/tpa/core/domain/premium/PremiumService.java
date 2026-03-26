package com.nexsol.tpa.core.domain.premium;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.client.InsuranceQuoteClient.PremiumCommand;
import com.nexsol.tpa.core.domain.client.PremiumProvider;
import com.nexsol.tpa.core.domain.plan.PlanFamily;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 보험료 산출 비즈니스 서비스 (Business Layer).
 * 요청 조립 → Provider 호출 → 도메인 결과 수신 흐름을 조율한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PremiumService {

    private final PremiumProvider premiumProvider;
    private final QuoteInsuredAssembler quoteAssembler;

    public Map<Long, Premium> calculateAll(PlanCondition cmd, List<PlanFamily> families) {
        return families.stream()
                .map(family -> new AbstractMap.SimpleEntry<>(family, calculate(cmd, family)))
                .filter(this::isCalculated)
                .collect(
                        Collectors.toMap(
                                e -> e.getKey().repPlan().id(),
                                Map.Entry::getValue,
                                (a, b) -> a,
                                LinkedHashMap::new));
    }

    public Premium calculateSingle(PlanCondition cmd, PlanFamily family, int repIdx) {
        Premium result = calculate(cmd, family);
        if (result == null) {
            throw new CoreException(
                    CoreErrorType.PREMIUM_CALCULATION_FAILED,
                    "familyId=" + family.familyId() + ", planCd=" + family.repPlan().planCode());
        }
        return result;
    }

    private Premium calculate(PlanCondition cmd, PlanFamily family) {
        PremiumCommand request = quoteAssembler.assemble(cmd, family.plans());
        if (request == null) {
            return null;
        }
        return premiumProvider.calculate(request);
    }

    private boolean isCalculated(Map.Entry<PlanFamily, Premium> entry) {
        if (entry.getValue() != null) {
            return true;
        }
        log.warn(
                "[PREMIUM] calculation skipped. familyId={}, planCd={}",
                entry.getKey().familyId(),
                entry.getKey().repPlan().planCode());
        return false;
    }
}

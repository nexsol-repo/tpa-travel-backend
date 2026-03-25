package com.nexsol.tpa.core.domain.premium;

import java.util.*;

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
        Map<Long, Premium> results = new LinkedHashMap<>();

        for (PlanFamily family : families) {
            Premium result = calculate(cmd, family);
            if (result == null) {
                log.warn(
                        "[PREMIUM] calculation skipped. familyId={}, planCd={}",
                        family.familyId(),
                        family.repPlan().planCode());
                continue;
            }
            results.put(family.repPlan().id(), result);
        }
        return results;
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
}

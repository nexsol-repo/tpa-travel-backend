package com.nexsol.tpa.core.domain.premium;

import java.util.*;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.quote.MeritzQuoteClient;
import com.nexsol.tpa.client.meritz.quote.MeritzQuoteClient.PremiumRequest;
import com.nexsol.tpa.core.domain.plan.PlanFamily;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 보험료 산출 비즈니스 서비스 (Business Layer).
 * 요청 생성 → API 호출 → 결과 변환 흐름을 조율한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PremiumService {

    private final MeritzQuoteClient quoteClient;
    private final QuoteInsuredAssembler quoteAssembler;
    private final QuoteResultComposer quoteResultComposer;
    private final ObjectMapper objectMapper;

    /**
     * 패밀리 목록에 대해 보험료를 일괄 산출한다.
     * key: repPlan.id, value: 산출 결과 (실패 시 skip).
     */
    public Map<Long, PremiumResult> calculateAll(PlanCondition cmd, List<PlanFamily> families) {
        int repIdx = cmd.representativeIndex() == null ? 0 : cmd.representativeIndex();
        Map<Long, PremiumResult> results = new LinkedHashMap<>();

        for (PlanFamily family : families) {
            JsonNode rawData = callApi(cmd, family);
            if (rawData == null) {
                log.warn(
                        "[PREMIUM] calculation skipped. familyId={}, planCd={}",
                        family.familyId(),
                        family.repPlan().planCode());
                continue;
            }
            results.put(
                    family.repPlan().id(), quoteResultComposer.compose(rawData, cmd, repIdx));
        }
        return results;
    }

    /**
     * 단일 패밀리에 대해 보험료를 산출한다.
     * 실손제외 등 단건 산출에 사용. 실패 시 예외 발생.
     */
    public PremiumResult calculateSingle(PlanCondition cmd, PlanFamily family, int repIdx) {
        JsonNode rawData = callApi(cmd, family);
        if (rawData == null) {
            throw new CoreException(
                    CoreErrorType.PREMIUM_CALCULATION_FAILED,
                    "familyId=" + family.familyId() + ", planCd=" + family.repPlan().planCode());
        }
        return quoteResultComposer.compose(rawData, cmd, repIdx);
    }

    // ── internal ──

    private JsonNode callApi(PlanCondition cmd, PlanFamily family) {
        PremiumRequest request = quoteAssembler.assemble(cmd, family.plans());
        if (request == null) {
            return null;
        }

        String rawJson = quoteClient.calculatePremium(request);
        if (rawJson == null) {
            return null;
        }
        return objectMapper.readTree(rawJson);
    }
}

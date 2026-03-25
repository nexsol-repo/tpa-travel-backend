package com.nexsol.tpa.core.domain.client;

import java.util.List;

/**
 * 외부 보험사 보험료 산출 API 통신 인터페이스.
 * raw JSON 반환. 파싱/변환은 PremiumProvider가 담당.
 */
public interface InsuranceQuoteClient {

    String calculatePremium(PremiumCommand command);

    record PremiumCommand(
            String company,
            String productCode,
            String unitProductCode,
            String sbcpDt,
            String insBgnDt,
            String insEdDt,
            String trvArCd,
            int representativeIndex,
            List<InsuredPersonCommand> insuredList) {

        public record InsuredPersonCommand(
                String planGroupCode,
                String planCode,
                String birth,
                String gender,
                String name,
                String nameEng) {}
    }
}

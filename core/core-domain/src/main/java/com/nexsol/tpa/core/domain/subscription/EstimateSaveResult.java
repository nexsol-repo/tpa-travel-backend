package com.nexsol.tpa.core.domain.subscription;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record EstimateSaveResult(
        boolean success,
        BigDecimal ttPrem,
        String polNo,
        String quotGrpNo,
        String quotReqNo,
        String errCd,
        String errMsg,
        Object rawData) {

    public static EstimateSaveResult success(
            BigDecimal ttPrem, String polNo, String quotGrpNo, String quotReqNo, Object rawData) {
        return new EstimateSaveResult(
                true, ttPrem, polNo, quotGrpNo, quotReqNo, null, null, rawData);
    }

    public static EstimateSaveResult fail(String errCd, String errMsg, Object rawData) {
        return new EstimateSaveResult(false, null, null, null, null, errCd, errMsg, rawData);
    }
}

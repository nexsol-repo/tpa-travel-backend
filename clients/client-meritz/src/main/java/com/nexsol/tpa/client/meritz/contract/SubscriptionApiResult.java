package com.nexsol.tpa.client.meritz.contract;

import java.math.BigDecimal;

public record SubscriptionApiResult(
        boolean success,
        BigDecimal ttPrem,
        String polNo,
        String quotGrpNo,
        String quotReqNo,
        String errCd,
        String errMsg,
        Object rawData) {

    public static SubscriptionApiResult success(
            BigDecimal ttPrem, String polNo, String quotGrpNo, String quotReqNo, Object rawData) {
        return new SubscriptionApiResult(
                true, ttPrem, polNo, quotGrpNo, quotReqNo, null, null, rawData);
    }

    public static SubscriptionApiResult fail(String errCd, String errMsg, Object rawData) {
        return new SubscriptionApiResult(false, null, null, null, null, errCd, errMsg, rawData);
    }
}

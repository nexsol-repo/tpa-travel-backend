package com.nexsol.tpa.core.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuoteResponse {

    private boolean ok;

    private String errCd;

    private String errMsg;

    private String rawErrMsg;

    private Period period;

    private Integer insuredCount;

    private Integer representativeIndex;

    private List<PlanCard> plans;

    @Data
    @Builder
    public static class Period {

        private String insBgnDt;

        private String insEdDt;

    }

    @Data
    @Builder
    public static class PlanCard {

        private Long planId;

        private String planGrpCd;

        private String planCd;

        private String planNm;

        private String planNmRaw;

        private Premium premium;

        private List<Coverage> coverages;

    }

    @Data
    @Builder
    public static class Premium {

        private long ttPrem;

        private String currency;

    }

    @Data
    @Builder
    public static class SelectedPlan {

        private Long planId;

        private String planGrpCd;

        private String planCd;

        private String planNm;

    }

    @Data
    @Builder
    public static class Coverage {

        private String covCd;

        private String covNm;

        private long insdAmt;

        private String cur;

        private List<Object> units;

    }

    public static QuoteResponse success(Period period, int representativeIndex, int insuredCount, List<PlanCard> plans) {
        return QuoteResponse.builder()
                .ok(true)
                .period(period)
                .representativeIndex(representativeIndex)
                .insuredCount(insuredCount)
                .plans(plans)
                .build();
    }

    public static QuoteResponse fail(String errCd, String errMsg, String rawErrMsg) {
        return QuoteResponse.builder()
                .ok(false)
                .errCd(errCd)
                .errMsg(errMsg)
                .rawErrMsg(rawErrMsg)
                .plans(List.of())
                .build();
    }
}

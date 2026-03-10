package com.nexsol.tpa.core.api.dto.v1;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

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

        private List<InsuredPremium> insuredPremiums;

        private String coverageTitle;

        private List<Coverage> coverages;

    }

    @Getter
    @Builder
    public static class InsuredPremium {

        private Integer index; // insuredList index

        private String currency; // "KRW"

        private Long ppsPrem; // 인당 보험료 (정수)

        private String birth;

        private String gndrCd; // 메리츠 응답 gndrCd (optional)

        private String cusNm; // optional

        private String cusEngNm; // optional

        private String ageBandCode; // optional

        private String ageBandLabel; // optional

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

        private List<CoverageUnit> units;

        private String categoryCode;

    }

    @Data
    @Builder
    public static class CoverageUnit {

        private String ageBandCode; // "AGE_15_69"

        private String ageBandLabel; // "15~69세"

        private Integer count; // 해당 연령대 인원수 (null = 보장없음)

        private Long insdAmt; // 해당 연령대 보장금액 (null = 보장없음)

        private Long premSum; // 해당 연령대 보험료 합 (null = 보장없음)

    }

    public static QuoteResponse success(Period period, int representativeIndex, int insuredCount,
            List<PlanCard> plans) {
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

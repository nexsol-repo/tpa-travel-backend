package com.nexsol.tpa.core.domain.premium;

import java.util.List;

public record PlanCondition(
        Long insurerId,
        String insBgnDt,
        String insEdDt,
        String trvArCd,
        Integer representativeIndex,
        List<Insured> insuredList,
        Boolean silsonExclude) {

    public PlanCondition(
            Long insurerId,
            String insBgnDt,
            String insEdDt,
            String trvArCd,
            Integer representativeIndex,
            List<Insured> insuredList) {
        this(insurerId, insBgnDt, insEdDt, trvArCd, representativeIndex, insuredList, false);
    }

    public record Insured(String birth, String gender) {}
}

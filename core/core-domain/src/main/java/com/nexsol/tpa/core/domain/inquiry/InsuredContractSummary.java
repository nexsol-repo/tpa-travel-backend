package com.nexsol.tpa.core.domain.inquiry;

import lombok.Builder;

@Builder
public record InsuredContractSummary(
        String polNo,
        String quotGrpNo,
        String quotReqNo,
        String sbcpDt,
        String insBgnDt,
        String insEdDt,
        String pdNm,
        String pdCd,
        String inspeNm,
        Integer ttInspeNum,
        String ttPrem,
        String stat) {}

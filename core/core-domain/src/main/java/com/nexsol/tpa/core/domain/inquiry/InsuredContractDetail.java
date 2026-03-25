package com.nexsol.tpa.core.domain.inquiry;

import lombok.Builder;

@Builder
public record InsuredContractDetail(
        String polNo,
        String quotGrpNo,
        String quotReqNo,
        String sbcpDt,
        String insBgnDt,
        String insEdDt,
        String pdNm,
        String pdCd,
        String ttPrem,
        String stat,
        String crdApvNo,
        String crdCncDt,
        String adjtYn) {}

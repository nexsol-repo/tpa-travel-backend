package com.nexsol.tpa.core.api.controller.v1.response;

import com.nexsol.tpa.core.domain.inquiry.InsuredContractDetail;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InsuredContractDetailResponse {

    private String polNo;
    private String quotGrpNo;
    private String quotReqNo;
    private String sbcpDt;
    private String insBgnDt;
    private String insEdDt;
    private String pdNm;
    private String pdCd;
    private String ttPrem;
    private String stat;
    private String crdApvNo;
    private String crdCncDt;
    private String adjtYn;

    public static InsuredContractDetailResponse of(InsuredContractDetail detail) {
        return InsuredContractDetailResponse.builder()
                .polNo(detail.polNo())
                .quotGrpNo(detail.quotGrpNo())
                .quotReqNo(detail.quotReqNo())
                .sbcpDt(detail.sbcpDt())
                .insBgnDt(detail.insBgnDt())
                .insEdDt(detail.insEdDt())
                .pdNm(detail.pdNm())
                .pdCd(detail.pdCd())
                .ttPrem(detail.ttPrem())
                .stat(detail.stat())
                .crdApvNo(detail.crdApvNo())
                .crdCncDt(detail.crdCncDt())
                .adjtYn(detail.adjtYn())
                .build();
    }
}

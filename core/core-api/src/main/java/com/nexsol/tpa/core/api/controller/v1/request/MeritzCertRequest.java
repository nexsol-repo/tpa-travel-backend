package com.nexsol.tpa.core.api.controller.v1.request;

import com.nexsol.tpa.core.domain.certificate.CertificateCommand;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MeritzCertRequest {

    private final Long contractId;
    private final String otptDiv;
    private final String otptTpCd;

    @Builder
    private MeritzCertRequest(Long contractId, String otptDiv, String otptTpCd) {
        this.contractId = contractId;
        this.otptDiv = otptDiv;
        this.otptTpCd = otptTpCd;
    }

    public CertificateCommand toCertificateCommand() {
        return new CertificateCommand(contractId, otptDiv, otptTpCd);
    }
}

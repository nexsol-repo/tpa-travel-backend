package com.nexsol.tpa.core.api.controller.v1.request;

import com.nexsol.tpa.core.domain.certificate.CertificateCommand;

public record CertificateRequest(Long contractId, String otptDiv, String otptTpCd) {

    public CertificateCommand toCertificateCommand() {
        return new CertificateCommand(contractId, otptDiv, otptTpCd);
    }
}

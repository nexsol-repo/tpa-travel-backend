package com.nexsol.tpa.core.domain.certificate;

public record CertificateCommand(Long contractId, String otptDiv, String otptTpCd) {}

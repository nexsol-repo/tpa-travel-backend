package com.nexsol.tpa.core.domain.auth;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.client.CertificationClient;
import com.nexsol.tpa.core.domain.client.CertificationClient.CertificationResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortOneCertificationReader {

    private final CertificationClient certificationClient;

    public CertificationResult getCertification(String impUid) {
        return certificationClient.getCertification(impUid);
    }
}

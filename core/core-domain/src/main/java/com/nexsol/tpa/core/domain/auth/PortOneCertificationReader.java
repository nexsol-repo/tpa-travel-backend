package com.nexsol.tpa.core.domain.auth;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.client.portone.PortOneClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortOneCertificationReader {

    private final PortOneClient portOneClient;

    public PortOneClient.CertificationResponse getCertification(String impUid) {
        return portOneClient.getCertification(impUid);
    }
}

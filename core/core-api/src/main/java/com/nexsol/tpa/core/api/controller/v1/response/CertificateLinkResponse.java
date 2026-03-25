package com.nexsol.tpa.core.api.controller.v1.response;

import com.nexsol.tpa.core.domain.certificate.CertificateLink;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CertificateLinkResponse {

    private String linkUrl;

    public static CertificateLinkResponse of(CertificateLink link) {
        return CertificateLinkResponse.builder().linkUrl(link.linkUrl()).build();
    }
}

package com.nexsol.tpa.core.domain.client;

import com.nexsol.tpa.core.domain.certificate.CertificateLink;

public interface CertificateProvider {

    CertificateLink issueCertificate(
            String company,
            String polNo,
            String pdCd,
            String quotGrpNo,
            String quotReqNo,
            String otptDiv,
            String otptTpCd);
}

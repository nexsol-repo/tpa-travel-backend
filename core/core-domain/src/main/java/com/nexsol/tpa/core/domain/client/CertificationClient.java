package com.nexsol.tpa.core.domain.client;

public interface CertificationClient {

    CertificationResult getCertification(String impUid);

    record CertificationResult(
            String impUid,
            String merchantUid,
            String uniqueKey,
            String name,
            String birthday,
            String gender,
            String phone,
            String rawJson) {}
}

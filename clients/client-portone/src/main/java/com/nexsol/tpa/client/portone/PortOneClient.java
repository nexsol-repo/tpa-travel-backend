package com.nexsol.tpa.client.portone;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.client.CertificationClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortOneClient implements CertificationClient {

    private final PortOneV1Properties props;
    private final PortOneFeign portOneFeign;

    private final ObjectMapper om = new ObjectMapper();

    private volatile CachedToken cachedToken;

    public String getAccessToken() {
        CachedToken ct = cachedToken;
        if (ct != null && ct.isValid()) return ct.accessToken;

        synchronized (this) {
            ct = cachedToken;
            if (ct != null && ct.isValid()) return ct.accessToken;

            String body =
                    portOneFeign.getToken(
                            Map.of(
                                    "imp_key",
                                    props.getApiKey(),
                                    "imp_secret",
                                    props.getApiSecret()));

            try {
                JsonNode root = om.readTree(body);
                int code = root.path("code").asInt(-1);
                if (code != 0) {
                    throw new RuntimeException(
                            "PortOne getToken failed. code="
                                    + code
                                    + ", message="
                                    + root.path("message").asText());
                }
                JsonNode res = root.path("response");
                String accessToken = res.path("access_token").asText();
                long now = Instant.now().getEpochSecond();
                long expiredAt = res.path("expired_at").asLong(now + 60);

                cachedToken = new CachedToken(accessToken, expiredAt - 30);
                return accessToken;
            } catch (Exception e) {
                throw new RuntimeException("PortOne getToken parse error", e);
            }
        }
    }

    @Override
    public CertificationResult getCertification(String impUid) {
        String token = getAccessToken();

        String body = portOneFeign.getCertification(impUid, token);

        try {
            JsonNode root = om.readTree(body);
            int code = root.path("code").asInt(-1);
            if (code != 0) {
                throw new RuntimeException(
                        "PortOne getCertification failed. code="
                                + code
                                + ", message="
                                + root.path("message").asText());
            }

            JsonNode res = root.path("response");
            return new CertificationResult(
                    res.path("imp_uid").asText(null),
                    res.path("merchant_uid").asText(null),
                    res.path("unique_key").asText(null),
                    res.path("name").asText(null),
                    res.path("birthday").asText(null),
                    res.path("gender").asText(null),
                    res.path("phone").asText(null),
                    body);
        } catch (Exception e) {
            throw new RuntimeException("PortOne getCertification parse error", e);
        }
    }

    private record CachedToken(String accessToken, long validUntilEpochSec) {
        boolean isValid() {
            return Instant.now().getEpochSecond() < validUntilEpochSec;
        }
    }
}

package com.nexsol.tpa.client.portone;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortOneClient {

    private final PortOneV1Properties props;

    private final ObjectMapper om = new ObjectMapper();

    private volatile CachedToken cachedToken;

    private WebClient webClient() {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /** 토큰 발급(캐시) */
    public String getAccessToken() {
        CachedToken ct = cachedToken;
        if (ct != null && ct.isValid()) return ct.accessToken;

        synchronized (this) {
            ct = cachedToken;
            if (ct != null && ct.isValid()) return ct.accessToken;

            String body =
                    webClient()
                            .post()
                            .uri("/users/getToken")
                            .bodyValue(
                                    Map.of(
                                            "imp_key",
                                            props.getApiKey(),
                                            "imp_secret",
                                            props.getApiSecret()))
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

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

    /** 본인인증 결과 조회 */
    public CertificationResponse getCertification(String impUid) {
        String token = getAccessToken();

        String body =
                webClient()
                        .get()
                        .uri("/certifications/{imp_uid}", impUid)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

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
            CertificationResponse r = new CertificationResponse();

            r.setImpUid(res.path("imp_uid").asText(null));
            r.setMerchantUid(res.path("merchant_uid").asText(null));
            r.setUniqueKey(res.path("unique_key").asText(null));
            r.setName(res.path("name").asText(null));
            r.setBirthday(res.path("birthday").asText(null));
            r.setGender(res.path("gender").asText(null));
            r.setPhone(res.path("phone").asText(null));

            r.setRawJson(body);
            return r;
        } catch (Exception e) {
            throw new RuntimeException("PortOne getCertification parse error", e);
        }
    }

    private record CachedToken(String accessToken, long validUntilEpochSec) {
        boolean isValid() {
            return Instant.now().getEpochSecond() < validUntilEpochSec;
        }
    }

    @Getter
    public static class CertificationResponse {

        private String impUid;

        private String merchantUid;

        private String uniqueKey;

        private String name;

        private String birthday;

        private String gender;

        private String phone;

        private String rawJson;

        public void setImpUid(String v) {
            this.impUid = v;
        }

        public void setMerchantUid(String v) {
            this.merchantUid = v;
        }

        public void setUniqueKey(String v) {
            this.uniqueKey = v;
        }

        public void setName(String v) {
            this.name = v;
        }

        public void setBirthday(String v) {
            this.birthday = v;
        }

        public void setGender(String v) {
            this.gender = v;
        }

        public void setPhone(String v) {
            this.phone = v;
        }

        public void setRawJson(String v) {
            this.rawJson = v;
        }
    }
}

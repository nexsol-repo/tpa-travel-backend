package com.nexsol.tpa.client.meritz.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeRequest;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
public class MeritzBridgeClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String bridgeBaseUrl;

    public MeritzBridgeClient(RestTemplate restTemplate, ObjectMapper objectMapper, String bridgeBaseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.bridgeBaseUrl = bridgeBaseUrl;
    }

    public MeritzBridgeResponse call(MeritzBridgeRequest req) {
        String url = bridgeBaseUrl + "/internal/meritz-bridge";

        req.setHeaders(
                req.getHeaders() == null
                        ? java.util.Map.of("Content-Type", "application/json; charset=UTF-8")
                        : filterBridgeForwardHeaders(req.getHeaders())
        );

        try {
            log.info("[MERITZ][BRIDGE][REQ] url={}, payload={}", url, objectMapper.writeValueAsString(req));
        } catch (Exception e) {
            log.info("[MERITZ][BRIDGE][REQ] url={}, payload=(json serialize fail) {}", url, req);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<MeritzBridgeRequest> entity = new HttpEntity<>(req, headers);

        ResponseEntity<MeritzBridgeResponse> res =
                restTemplate.exchange(url, HttpMethod.POST, entity, MeritzBridgeResponse.class);

        MeritzBridgeResponse body = res.getBody();

        try {
            log.info("[MERITZ][BRIDGE][RES] httpStatus={}, payload={}",
                    res.getStatusCode(), objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            log.info("[MERITZ][BRIDGE][RES] httpStatus={}, payload=(json serialize fail) {}",
                    res.getStatusCode(), body);
        }

        if (body == null) {
            throw new IllegalStateException("Bridge response body is null");
        }
        return body;
    }

    private java.util.Map<String, String> filterBridgeForwardHeaders(java.util.Map<String, String> in) {
        java.util.LinkedHashMap<String, String> out = new java.util.LinkedHashMap<>();
        for (var e : in.entrySet()) {
            String k = e.getKey();
            if (k == null) continue;
            String keyLower = k.toLowerCase();

            if (keyLower.equals("authorization")) continue;
            if (keyLower.equals("x-api-tran-id")) continue;
            if (keyLower.equals("timestamp")) continue;

            out.put(k, e.getValue());
        }

        // 최소 Content-Type 보장
        out.putIfAbsent("Content-Type", "application/json; charset=UTF-8");
        return out;
    }

    public <T> T parseInnerBody(MeritzBridgeResponse bridgeResponse, Class<T> clazz) {
        try {
            return objectMapper.readValue(bridgeResponse.getBody(), clazz);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse meritz inner body json", e);
        }
    }
}


package com.nexsol.tpa.client.meritz.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexsol.tpa.client.meritz.bridge.MeritzBridgeClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@EnableConfigurationProperties(MeritzTpaProperties.class)
public class MeritzClientsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestTemplate meritzRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public MeritzBridgeClient meritzBridgeClient(RestTemplate meritzRestTemplate, ObjectMapper objectMapper,
            MeritzBridgeProperties bridgeProps) {
        return new MeritzBridgeClient(meritzRestTemplate, objectMapper, bridgeProps.getBaseUrl());
    }

    @Bean
    public MeritzBridgeProperties meritzBridgeProperties() {
        return new MeritzBridgeProperties();
    }

    public static class MeritzBridgeProperties {

        // core-api에서 설정한 meritz.bridge.base-url 을 그대로 쓰고 싶으면
        // prefix를 맞추는게 제일 깔끔함.
        private String baseUrl = "http://211.220.195.16:18081";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

    }

}

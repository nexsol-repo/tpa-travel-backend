package com.nexsol.tpa.core.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "portone.v1")
public class PortOneV1Properties {

    private String baseUrl;

    private String apiKey;

    private String apiSecret;

    private int timeoutMs = 5000;

}

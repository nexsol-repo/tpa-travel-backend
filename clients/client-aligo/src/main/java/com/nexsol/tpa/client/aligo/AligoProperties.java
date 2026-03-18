package com.nexsol.tpa.client.aligo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Validated
@ConfigurationProperties(prefix = "aligo")
public class AligoProperties {

    private String apiKey;

    private String userId;

    private String sender;
}

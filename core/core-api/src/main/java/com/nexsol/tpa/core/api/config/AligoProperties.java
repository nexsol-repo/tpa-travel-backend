package com.nexsol.tpa.core.api.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "aligo")
public class AligoProperties {
    private String apiKey;   // ALIGO_API_KEY
    private String userId;   // ALIGO_USER_ID
    private String sender;   // ALIGO_SENDER (발신번호)
}


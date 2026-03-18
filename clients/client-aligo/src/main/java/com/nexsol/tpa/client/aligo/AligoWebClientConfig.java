package com.nexsol.tpa.client.aligo;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(AligoProperties.class)
public class AligoWebClientConfig {

    @Bean(name = "aligoWebClient")
    public WebClient aligoWebClient() {
        return WebClient.builder().build();
    }
}

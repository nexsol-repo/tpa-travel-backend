package com.nexsol.tpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.nexsol.tpa")
@ConfigurationPropertiesScan(basePackages = "com.nexsol.tpa.core.api")
@EnableConfigurationProperties(com.nexsol.tpa.client.meritz.config.CompaniesConfigsProperties.class)
public class CoreApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreApiApplication.class, args);
    }
}

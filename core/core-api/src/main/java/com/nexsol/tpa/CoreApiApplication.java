package com.nexsol.tpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.nexsol.tpa")
@EnableJpaRepositories(basePackages = "com.nexsol.tpa.core.api.repository")
@EntityScan(basePackages = "com.nexsol.tpa.core.api.entity")
@EnableConfigurationProperties(com.nexsol.tpa.core.api.meritz.config.CompaniesConfigsProperties.class)
public class CoreApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreApiApplication.class, args);
    }

}

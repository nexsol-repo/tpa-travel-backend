package com.nexsol.tpa.client.aligo;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.nexsol.tpa.client.aligo")
@EnableConfigurationProperties(AligoProperties.class)
public class AligoAutoConfiguration {}

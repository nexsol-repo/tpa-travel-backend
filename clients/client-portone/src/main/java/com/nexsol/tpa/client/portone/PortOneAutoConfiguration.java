package com.nexsol.tpa.client.portone;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.nexsol.tpa.client.portone")
@EnableConfigurationProperties(PortOneV1Properties.class)
public class PortOneAutoConfiguration {}

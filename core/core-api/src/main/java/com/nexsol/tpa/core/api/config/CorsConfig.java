package com.nexsol.tpa.core.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:3001", "https://otmi.tpa-mall.insboon.com",
                    "https://dev-otmi.tpa-mall.insboon1.com","https://dev-travel-tpa.nexsol.ai","https://travel.tpakorea.com","https://dev-admin.tpa.nexsol.ai","https://manage.tpakorea.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }

}

package com.nexsol.tpa.client.meritz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "companies.configs.tpa")
public class MeritzTpaProperties {

    private String companyCode; // "TPA"

    private String aflcoDivCd; // "TPA" 등

    private String clientId;

    private String clientSecret;

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getAflcoDivCd() {
        return aflcoDivCd;
    }

    public void setAflcoDivCd(String aflcoDivCd) {
        this.aflcoDivCd = aflcoDivCd;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

}

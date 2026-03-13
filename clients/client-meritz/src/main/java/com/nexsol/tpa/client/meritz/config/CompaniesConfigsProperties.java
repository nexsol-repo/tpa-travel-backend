package com.nexsol.tpa.client.meritz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "companies.configs")
public class CompaniesConfigsProperties {

    private final CompanyConfig tpa;

    private final CompanyConfig insboon;

    public CompaniesConfigsProperties(CompanyConfig tpa, CompanyConfig insboon) {
        this.tpa = tpa;
        this.insboon = insboon;
    }

    public CompanyConfig getTpa() {
        return tpa;
    }

    public CompanyConfig getInsboon() {
        return insboon;
    }

    public CompanyConfig resolve(String company) {
        if (company == null || company.isBlank() || "TPA".equalsIgnoreCase(company)) {
            return tpa;
        }
        if ("INSBOON".equalsIgnoreCase(company)) {
            return insboon;
        }
        throw new IllegalArgumentException("Unknown company: " + company);
    }

    public static class CompanyConfig {

        private final String hostUrl;

        private final String clientId;

        private final String clientSecret;

        private final String companyCode;

        private final String aflcoDivCd;

        private final String gnrAflcoCd;

        private final String bizpeNo;

        private final String polNo;

        public CompanyConfig(
                String hostUrl,
                String clientId,
                String clientSecret,
                String companyCode,
                String aflcoDivCd,
                String gnrAflcoCd,
                String bizpeNo,
                String polNo) {
            this.hostUrl = hostUrl;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.companyCode = companyCode;
            this.aflcoDivCd = aflcoDivCd;
            this.gnrAflcoCd = gnrAflcoCd;
            this.bizpeNo = bizpeNo;
            this.polNo = polNo;
        }

        public String getHostUrl() {
            return hostUrl;
        }

        public String getClientId() {
            return clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public String getCompanyCode() {
            return companyCode;
        }

        public String getAflcoDivCd() {
            return aflcoDivCd;
        }

        public String getGnrAflcoCd() {
            return gnrAflcoCd;
        }

        public String getBizpeNo() {
            return bizpeNo;
        }

        public String getPolNo() {
            return polNo;
        }
    }
}

package com.nexsol.tpa.core.api.meritz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "companies.configs")
public class CompaniesConfigsProperties {

    private CompanyConfig tpa;

    private CompanyConfig insboon;

    public CompanyConfig getTpa() {
        return tpa;
    }

    public void setTpa(CompanyConfig tpa) {
        this.tpa = tpa;
    }

    public CompanyConfig getInsboon() {
        return insboon;
    }

    public void setInsboon(CompanyConfig insboon) {
        this.insboon = insboon;
    }

    public static class CompanyConfig {

        private String hostUrl;

        private String clientId;

        private String clientSecret;

        private String companyCode;

        private String aflcoDivCd;

        private String gnrAflcoCd;

        private String bizpeNo;

        public String getHostUrl() {
            return hostUrl;
        }

        public void setHostUrl(String hostUrl) {
            this.hostUrl = hostUrl;
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

        public String getGnrAflcoCd() {
            return gnrAflcoCd;
        }

        public void setGnrAflcoCd(String gnrAflcoCd) {
            this.gnrAflcoCd = gnrAflcoCd;
        }

        public String getBizpeNo() {
            return bizpeNo;
        }

        public void setBizpeNo(String bizpeNo) {
            this.bizpeNo = bizpeNo;
        }

    }

}

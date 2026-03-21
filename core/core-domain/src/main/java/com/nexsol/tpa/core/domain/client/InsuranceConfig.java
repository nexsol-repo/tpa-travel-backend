package com.nexsol.tpa.core.domain.client;

public interface InsuranceConfig {

    CompanyInfo resolveInfo(String company);

    CompanyInfo getTpaInfo();

    record CompanyInfo(String companyCode, String polNo) {}
}

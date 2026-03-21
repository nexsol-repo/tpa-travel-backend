package com.nexsol.tpa.core.domain.client;

public interface InsuranceReferenceClient {

    InsuranceContractClient.BridgeApiResult getPlans(String stdDt);

    InsuranceContractClient.BridgeApiResult getCityNationCodes(String keyword, String type);
}

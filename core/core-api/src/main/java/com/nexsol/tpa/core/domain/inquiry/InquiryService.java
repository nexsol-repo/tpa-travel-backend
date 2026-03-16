package com.nexsol.tpa.core.domain.inquiry;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeApiResponse;
import com.nexsol.tpa.client.meritz.contract.MeritzContractClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final MeritzContractClient meritzClient;

    public MeritzBridgeApiResponse contractList(String company, Map<String, Object> bodyFields) {
        return meritzClient.contractList(company, bodyFields);
    }

    public MeritzBridgeApiResponse contractDetail(String company, Map<String, Object> bodyFields) {
        return meritzClient.contractDetail(company, bodyFields);
    }
}

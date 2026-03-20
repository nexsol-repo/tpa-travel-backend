package com.nexsol.tpa.core.domain.inquiry;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeApiResponse;
import com.nexsol.tpa.client.meritz.contract.MeritzContractClient;
import com.nexsol.tpa.core.api.controller.v1.request.ContractInquiryRequest;
import com.nexsol.tpa.core.api.controller.v1.request.ContractListRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final MeritzContractClient meritzClient;

    public MeritzBridgeApiResponse contractList(String company, ContractListRequest request) {
        Map<String, Object> bodyFields = new LinkedHashMap<>();
        if (request.polNo() != null) bodyFields.put("polNo", request.polNo());
        if (request.quotReqNo() != null) bodyFields.put("quotReqNo", request.quotReqNo());
        if (request.ctrStDt() != null) bodyFields.put("ctrStDt", request.ctrStDt());
        if (request.ctrEdDt() != null) bodyFields.put("ctrEdDt", request.ctrEdDt());
        return meritzClient.contractList(company, bodyFields);
    }

    public MeritzBridgeApiResponse contractDetail(String company, ContractInquiryRequest request) {
        Map<String, Object> bodyFields = new LinkedHashMap<>();
        if (request.polNo() != null) bodyFields.put("polNo", request.polNo());
        if (request.ctrNo() != null) bodyFields.put("ctrNo", request.ctrNo());
        return meritzClient.contractDetail(company, bodyFields);
    }
}

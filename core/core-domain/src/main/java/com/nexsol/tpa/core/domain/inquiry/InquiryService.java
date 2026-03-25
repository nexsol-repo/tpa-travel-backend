package com.nexsol.tpa.core.domain.inquiry;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.client.InquiryProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryProvider inquiryProvider;

    public List<InsuredContractSummary> contractList(
            String company, Map<String, Object> bodyFields) {
        return inquiryProvider.contractList(company, bodyFields);
    }

    public InsuredContractDetail contractDetail(String company, Map<String, Object> bodyFields) {
        return inquiryProvider.contractDetail(company, bodyFields);
    }
}

package com.nexsol.tpa.core.domain.client;

import java.util.List;
import java.util.Map;

import com.nexsol.tpa.core.domain.inquiry.InsuredContractDetail;
import com.nexsol.tpa.core.domain.inquiry.InsuredContractSummary;

public interface InquiryProvider {
    List<InsuredContractSummary> contractList(String company, Map<String, Object> bodyFields);

    InsuredContractDetail contractDetail(String company, Map<String, Object> bodyFields);
}

package com.nexsol.tpa.core.api.controller.v1.request;

import java.util.LinkedHashMap;
import java.util.Map;

public record ContractInquiryRequest(String polNo, String ctrNo) {

    public Map<String, Object> toBodyFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        if (polNo != null) fields.put("polNo", polNo);
        if (ctrNo != null) fields.put("ctrNo", ctrNo);
        return fields;
    }
}

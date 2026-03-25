package com.nexsol.tpa.core.api.controller.v1.request;

import java.util.LinkedHashMap;
import java.util.Map;

public record ContractListRequest(String polNo, String quotReqNo, String ctrStDt, String ctrEdDt) {

    public Map<String, Object> toBodyFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        if (polNo != null) fields.put("polNo", polNo);
        if (quotReqNo != null) fields.put("quotReqNo", quotReqNo);
        if (ctrStDt != null) fields.put("ctrStDt", ctrStDt);
        if (ctrEdDt != null) fields.put("ctrEdDt", ctrEdDt);
        return fields;
    }
}

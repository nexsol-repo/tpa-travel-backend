package com.nexsol.tpa.core.api.service.v1.dto;

import java.util.Map;

public class MeritzHndyPremCmptRequest {

    /** 회사코드 (default: TPA) */
    private String company = "TPA";

    /**
     * Meritz body에 들어갈 값들. 엑셀 명세 확정되면 필드로 승격시키면 됨.
     */
    private Map<String, Object> body;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

}

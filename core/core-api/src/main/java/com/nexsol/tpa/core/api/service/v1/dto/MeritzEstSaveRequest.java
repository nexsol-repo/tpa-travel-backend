package com.nexsol.tpa.core.api.service.v1.dto;

import java.util.Map;

public class MeritzEstSaveRequest {

    private String company = "TPA";

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

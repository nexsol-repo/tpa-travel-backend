package com.nexsol.tpa.core.api.dto.v1;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeritzCertRequest {

    /** 내부 계약 ID (travel_contract.id) */
    private Long contractId;

    /** 출력구분: A(가입증명서/국문), B(피보험자별/영문+국문) */
    private String otptDiv;

    /** 출력유형코드: V(Viewer), D(Download) */
    private String otptTpCd;

}

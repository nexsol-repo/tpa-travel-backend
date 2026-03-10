package com.nexsol.tpa.core.api.dto.v1;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class BaseResponse {

    private boolean ok;

    private String errCd;

    private String errMsg;

}

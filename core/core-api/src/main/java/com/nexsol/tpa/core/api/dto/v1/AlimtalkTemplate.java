package com.nexsol.tpa.core.api.dto.v1;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlimtalkTemplate {
    TRAVEL_CONTRACT_COMPLETED("TPL_XXXXXX"); // <- 알리고에서 발급한 tpl_code로 교체

    private final String tplCode;
}


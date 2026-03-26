package com.nexsol.tpa.core.api.controller.v1.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardCancelRequest {

    private String polNo;

    private String estNo;

    private String orgApvNo;

    private String cncAmt;
}

package com.nexsol.tpa.core.api.controller.v1;

import org.springframework.web.bind.annotation.*;

import com.nexsol.tpa.core.api.controller.v1.request.CardApproveRequest;
import com.nexsol.tpa.core.api.controller.v1.request.CardCancelRequest;
import com.nexsol.tpa.core.domain.payment.CardApproval;
import com.nexsol.tpa.core.domain.payment.CardCancellation;
import com.nexsol.tpa.core.domain.payment.PaymentService;
import com.nexsol.tpa.core.support.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/travel")
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/payments/cards/approve")
    public ApiResponse<CardApproval> approveCard(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody CardApproveRequest request) {
        CardApproval result =
                service.approveCard(
                        company,
                        request.getPolNo(),
                        request.getQuotGrpNo(),
                        request.getQuotReqNo(),
                        request.getCrdNo(),
                        request.getEfctPrd(),
                        request.getDporNm(),
                        request.getDporCd(),
                        request.getRcptPrem());
        return ApiResponse.success(result);
    }

    @PostMapping("/payments/cards/cancel")
    public ApiResponse<CardCancellation> cancelCard(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody CardCancelRequest request) {
        CardCancellation result =
                service.cancelCard(
                        company,
                        request.getPolNo(),
                        request.getEstNo(),
                        request.getOrgApvNo(),
                        request.getCncAmt());
        return ApiResponse.success(result);
    }
}

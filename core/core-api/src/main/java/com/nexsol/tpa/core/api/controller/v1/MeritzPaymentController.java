package com.nexsol.tpa.core.api.controller.v1;

import org.springframework.web.bind.annotation.*;

import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeApiResponse;
import com.nexsol.tpa.core.api.controller.v1.request.MeritzCardApproveRequest;
import com.nexsol.tpa.core.api.controller.v1.request.MeritzCardCancelRequest;
import com.nexsol.tpa.core.domain.payment.MeritzPaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/meritz")
public class MeritzPaymentController {

    private final MeritzPaymentService service;

    @PostMapping("/payments/cards/approve")
    public MeritzBridgeApiResponse approveCard(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody MeritzCardApproveRequest request) {
        return service.approveCard(
                company,
                request.getPolNo(),
                request.getQuotGrpNo(),
                request.getQuotReqNo(),
                request.getCrdNo(),
                request.getEfctPrd(),
                request.getDporNm(),
                request.getDporCd(),
                request.getRcptPrem());
    }

    @PostMapping("/payments/cards/cancel")
    public MeritzBridgeApiResponse cancelCard(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody MeritzCardCancelRequest request) {
        return service.cancelCard(
                company,
                request.getPolNo(),
                request.getEstNo(),
                request.getOrgApvNo(),
                request.getCncAmt());
    }
}

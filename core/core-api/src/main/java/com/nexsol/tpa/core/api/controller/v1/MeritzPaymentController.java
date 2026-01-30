package com.nexsol.tpa.core.api.controller.v1;

import com.nexsol.tpa.core.api.meritz.dto.payment.MeritzCardApproveBody;
import com.nexsol.tpa.core.api.meritz.dto.payment.MeritzCardCancelBody;
import com.nexsol.tpa.core.api.service.v1.MeritzPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/meritz")
public class MeritzPaymentController {

    private final MeritzPaymentService service;

    @PostMapping("/payments/cards/approve")
    public String approveCard(@RequestParam(defaultValue = "TPA") String companyCode,
            @RequestBody MeritzCardApproveBody request) {
        return service.approveCard(companyCode, request);
    }

    @PostMapping("/payments/cards/cancel")
    public String cancelCard(@RequestParam(defaultValue = "TPA") String companyCode,
            @RequestBody MeritzCardCancelBody request) {
        return service.cancelCard(companyCode, request);
    }

}

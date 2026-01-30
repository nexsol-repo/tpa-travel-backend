package com.nexsol.tpa.core.api.controller.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/meritz")
public class MeritzBackofficeController {

    // private final MeritzBackofficeService service;

    /** 정산 목록 조회 (Meritz: /adjtLstInq) */
    @GetMapping("/settlements")
    public Object listSettlements(/* @RequestParam ... */) {
        // return service.listSettlements(...);
        return null;
    }

    /** 통합 고객 전환 (Meritz: /trvItgrCusConv) */
    @PostMapping("/customers/convert")
    public Object convertCustomer(@RequestBody Object request) {
        // return service.convertCustomer(request);
        return null;
    }

    /** 계약 취소 (Meritz: /trvCtrCcl) */
    @PostMapping("/contracts/{contractNo}/cancel")
    public Object cancelContract(@PathVariable String contractNo, @RequestBody Object request) {
        // return service.cancelContract(contractNo, request);
        return null;
    }

}

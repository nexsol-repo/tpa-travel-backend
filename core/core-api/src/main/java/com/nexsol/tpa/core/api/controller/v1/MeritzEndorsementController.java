package com.nexsol.tpa.core.api.controller.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/meritz")
public class MeritzEndorsementController {

    // private final MeritzEndorsementService service;

    /** (문서상) 계약 변경 (Meritz: /trvChangeCtr) */
    @PostMapping("/contracts/{contractNo}/change")
    public Object changeContract(@PathVariable String contractNo, @RequestBody Object request) {
        // return service.changeContract(contractNo, request);
        return null;
    }

    /** 배서 보험료 계산 (Meritz: /calculateOpapiTrvEndrPrem) */
    @PostMapping("/endorsements/premium")
    public Object calcEndorsementPremium(@RequestBody Object request) {
        // return service.calcEndorsementPremium(request);
        return null;
    }

    /** 배서 체결 저장 (Meritz: /saveOpapiTrvEndrCclu) */
    @PostMapping("/endorsements")
    public Object saveEndorsement(@RequestBody Object request) {
        // return service.saveEndorsement(request);
        return null;
    }

}

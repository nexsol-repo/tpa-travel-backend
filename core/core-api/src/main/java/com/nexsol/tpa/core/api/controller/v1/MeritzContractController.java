package com.nexsol.tpa.core.api.controller.v1;

import com.nexsol.tpa.core.api.dto.v1.MeritzCertRequest;
import com.nexsol.tpa.core.api.dto.v1.contract.*;
import com.nexsol.tpa.core.api.meritz.dto.contract.MeritzCtrLstInqBody;
import com.nexsol.tpa.core.api.meritz.dto.contract.MeritzTrvCtrInqBody;
import com.nexsol.tpa.core.api.service.v1.MeritzContractService;
import com.nexsol.tpa.core.api.service.v1.TravelContractQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/meritz")
public class MeritzContractController {

    private final MeritzContractService service;

    private final TravelContractQueryService queryService;

    /** 계약 목록 조회 (Meritz: /ctrLstInq) */
    @GetMapping("/contracts/list")
    public String contractList(@RequestParam(defaultValue = "TPA") String companyCode,
            @RequestBody MeritzCtrLstInqBody request) {
        return service.contractList(companyCode, request);
    }

    /** 계약 조회 (Meritz: /trvCtrInq) */
    @PostMapping("/contracts/inquiry")
    public String contractInquiry(@RequestParam(defaultValue = "TPA") String companyCode,
            @RequestBody MeritzTrvCtrInqBody request) {
        return service.contractInquiry(companyCode, request);
    }

    /** 가입증명서 출력 */
    @PostMapping("/contracts/certificate")
    public String joinCertificate(@RequestParam(defaultValue = "TPA") String companyCode,
            @RequestBody MeritzCertRequest request) {
        return service.joinCertificate(companyCode, request);
    }

    /** 여행자보험 접수 */
    @PostMapping("/travel/contract/apply")
    public ContractApplyResponse apply(@RequestBody ContractApplyRequest request) {
        return service.apply(request);
    }

    /** 여행자보험 가입(결제) */
    @PostMapping("/travel/contract/completed")
    public ContractCompletedResponse completed(
            @RequestParam(defaultValue = "TPA") String companyCode,
            @RequestBody ContractCompletedRequest request
    ) {
        return service.completed(companyCode, request);
    }

    /** 여행자보험 가입(결제취소) */
    @PostMapping("/travel/contract/cancel")
    public ContractCancelResponse cancel(
            @RequestParam(defaultValue = "TPA") String companyCode,
            @RequestBody ContractCancelRequest request
    ) {
        return service.cancel(companyCode, request);
    }

    /** 계약 목록 (auth_unique_key 최신순 기본) */
    @GetMapping("/travel/contracts")
    public Page<TravelContractQueryDto.ContractListItem> list(
            @RequestParam(required = false) String authUniqueKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return queryService.list(authUniqueKey, page, size);
    }

    /** 계약 단건 조회 (id) */
    @GetMapping("/travel/contracts/{id}")
    public TravelContractQueryDto.ContractDetail get(@PathVariable Long id) {
        return queryService.get(id);
    }

}

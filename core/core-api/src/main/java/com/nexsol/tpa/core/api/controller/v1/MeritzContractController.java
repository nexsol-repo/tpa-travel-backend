package com.nexsol.tpa.core.api.controller.v1;

import org.springframework.web.bind.annotation.*;

import com.nexsol.tpa.core.api.controller.v1.request.ContractApplyRequest;
import com.nexsol.tpa.core.api.controller.v1.request.ContractCancelRequest;
import com.nexsol.tpa.core.api.controller.v1.request.ContractCompletedRequest;
import com.nexsol.tpa.core.api.controller.v1.request.ContractInquiryRequest;
import com.nexsol.tpa.core.api.controller.v1.request.ContractListRequest;
import com.nexsol.tpa.core.api.controller.v1.request.MeritzCertRequest;
import com.nexsol.tpa.core.api.controller.v1.response.ContractCompletedResponse;
import com.nexsol.tpa.core.domain.apply.ApplyService;
import com.nexsol.tpa.core.domain.cancel.CancelService;
import com.nexsol.tpa.core.domain.certificate.CertificateService;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient.BridgeApiResult;
import com.nexsol.tpa.core.domain.inquiry.InquiryService;
import com.nexsol.tpa.core.domain.subscription.SubscriptionService;
import com.nexsol.tpa.core.support.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/meritz")
public class MeritzContractController {

    private final ApplyService applyService;
    private final SubscriptionService subscriptionService;
    private final CancelService cancelService;
    private final CertificateService certificateService;
    private final InquiryService inquiryService;

    /** 계약 목록 조회 (Meritz: /ctrLstInq) */
    @GetMapping("/contracts/list")
    public BridgeApiResult contractList(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody ContractListRequest request) {
        return inquiryService.contractList(company, request);
    }

    /** 계약 조회 (Meritz: /trvCtrInq) */
    @PostMapping("/contracts/inquiry")
    public BridgeApiResult contractInquiry(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody ContractInquiryRequest request) {
        return inquiryService.contractDetail(company, request);
    }

    /** 가입증명서 출력 */
    @PostMapping("/contracts/certificate")
    public BridgeApiResult joinCertificate(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody MeritzCertRequest request) {
        return certificateService.issue(company, request.toCertificateCommand());
    }

    /** 여행자보험 접수 */
    @PostMapping("/travel/contract/apply")
    public ApiResponse<Long> apply(@RequestBody ContractApplyRequest request) {
        return ApiResponse.success(applyService.apply(request.toCommand()));
    }

    /** 여행자보험 가입(결제) */
    @PostMapping("/travel/contract/completed")
    public ApiResponse<ContractCompletedResponse> completed(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody ContractCompletedRequest request) {
        var result = subscriptionService.subscribe(company, request.toSubscriptionCommand());
        return ApiResponse.success(ContractCompletedResponse.of(result));
    }

    /** 여행자보험 가입(결제취소) */
    @PostMapping("/travel/contract/cancel")
    public ApiResponse<Long> cancel(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody ContractCancelRequest request) {
        Long contractId = cancelService.cancel(company, request.toRefundCommand());
        return ApiResponse.success(contractId);
    }
}

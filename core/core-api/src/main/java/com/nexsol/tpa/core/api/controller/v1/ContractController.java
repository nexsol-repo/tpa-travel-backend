package com.nexsol.tpa.core.api.controller.v1;

import org.springframework.web.bind.annotation.*;

import com.nexsol.tpa.core.api.controller.v1.request.CertificateRequest;
import com.nexsol.tpa.core.api.controller.v1.request.ContractApplyRequest;
import com.nexsol.tpa.core.api.controller.v1.request.ContractCancelRequest;
import com.nexsol.tpa.core.api.controller.v1.request.ContractCompletedRequest;
import com.nexsol.tpa.core.api.controller.v1.request.ContractInquiryRequest;
import com.nexsol.tpa.core.api.controller.v1.request.ContractListRequest;
import com.nexsol.tpa.core.api.controller.v1.response.CertificateLinkResponse;
import com.nexsol.tpa.core.api.controller.v1.response.ContractCompletedResponse;
import com.nexsol.tpa.core.api.controller.v1.response.ContractDetailResponse;
import com.nexsol.tpa.core.api.controller.v1.response.ContractListItem;
import com.nexsol.tpa.core.api.controller.v1.response.InsuredContractDetailResponse;
import com.nexsol.tpa.core.api.controller.v1.response.InsuredContractListResponse;
import com.nexsol.tpa.core.domain.apply.ApplyService;
import com.nexsol.tpa.core.domain.cancel.CancelService;
import com.nexsol.tpa.core.domain.certificate.CertificateService;
import com.nexsol.tpa.core.domain.contract.TravelContractQueryService;
import com.nexsol.tpa.core.domain.inquiry.InquiryService;
import com.nexsol.tpa.core.domain.subscription.SubscriptionService;
import com.nexsol.tpa.core.support.PageResult;
import com.nexsol.tpa.core.support.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/travel")
public class ContractController {

    private final ApplyService applyService;
    private final SubscriptionService subscriptionService;
    private final CancelService cancelService;
    private final CertificateService certificateService;
    private final InquiryService inquiryService;
    private final TravelContractQueryService queryService;

    // ── 계약 조회 (우리 DB) ──

    @GetMapping("/contracts")
    public ApiResponse<PageResult<ContractListItem>> list(
            @RequestParam(required = false) String authUniqueKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = queryService.list(authUniqueKey, page, size);
        return ApiResponse.success(result);
    }

    @GetMapping("/contracts/{id}")
    public ApiResponse<ContractDetailResponse> get(@PathVariable Long id) {
        var detail = queryService.get(id);
        return ApiResponse.success(detail);
    }

    // ── 계약 조회 (보험사 API) ──

    @GetMapping("/contracts/list")
    public ApiResponse<InsuredContractListResponse> contractList(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody ContractListRequest request) {
        var result = inquiryService.contractList(company, request.toBodyFields());
        return ApiResponse.success(InsuredContractListResponse.of(result));
    }

    @PostMapping("/contracts/inquiry")
    public ApiResponse<InsuredContractDetailResponse> contractInquiry(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody ContractInquiryRequest request) {
        var result = inquiryService.contractDetail(company, request.toBodyFields());
        return ApiResponse.success(InsuredContractDetailResponse.of(result));
    }

    // ── 가입증명서 ──

    @PostMapping("/contracts/certificate")
    public ApiResponse<CertificateLinkResponse> joinCertificate(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody CertificateRequest request) {
        var result = certificateService.issue(company, request.toCertificateCommand());
        return ApiResponse.success(CertificateLinkResponse.of(result));
    }

    // ── 계약 생성/결제/취소 ──

    @PostMapping("/contract/apply")
    public ApiResponse<Long> apply(@RequestBody ContractApplyRequest request) {
        Long contractId = applyService.apply(request.toCommand());
        return ApiResponse.success(contractId);
    }

    @PostMapping("/contract/completed")
    public ApiResponse<ContractCompletedResponse> completed(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody ContractCompletedRequest request) {
        var result = subscriptionService.subscribe(company, request.toSubscriptionCommand());
        return ApiResponse.success(ContractCompletedResponse.of(result));
    }

    @PostMapping("/contract/cancel")
    public ApiResponse<Long> cancel(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody ContractCancelRequest request) {
        Long contractId = cancelService.cancel(company, request.toRefundCommand());
        return ApiResponse.success(contractId);
    }
}

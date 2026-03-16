package com.nexsol.tpa.core.api.controller.v1;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeApiResponse;
import com.nexsol.tpa.client.meritz.dto.contract.MeritzCtrLstInqBody;
import com.nexsol.tpa.client.meritz.dto.contract.MeritzTrvCtrInqBody;
import com.nexsol.tpa.core.api.controller.v1.request.ContractApplyRequest;
import com.nexsol.tpa.core.api.controller.v1.request.ContractCancelRequest;
import com.nexsol.tpa.core.api.controller.v1.request.ContractCompletedRequest;
import com.nexsol.tpa.core.api.controller.v1.request.MeritzCertRequest;
import com.nexsol.tpa.core.api.controller.v1.response.ContractApplyResponse;
import com.nexsol.tpa.core.api.controller.v1.response.ContractCancelResponse;
import com.nexsol.tpa.core.api.controller.v1.response.ContractCompletedResponse;
import com.nexsol.tpa.core.domain.apply.ApplyCommand;
import com.nexsol.tpa.core.domain.apply.ApplyService;
import com.nexsol.tpa.core.domain.cancel.CancelCommand;
import com.nexsol.tpa.core.domain.cancel.CancelService;
import com.nexsol.tpa.core.domain.certificate.CertificateCommand;
import com.nexsol.tpa.core.domain.certificate.CertificateService;
import com.nexsol.tpa.core.domain.inquiry.InquiryService;
import com.nexsol.tpa.core.domain.subscription.SubscriptionCommand;
import com.nexsol.tpa.core.domain.subscription.SubscriptionService;

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
    public MeritzBridgeApiResponse contractList(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody MeritzCtrLstInqBody request) {
        Map<String, Object> bodyFields = new LinkedHashMap<>();
        if (request.body() != null) {
            MeritzCtrLstInqBody.Body b = request.body();
            if (b.polNo() != null) bodyFields.put("polNo", b.polNo());
            if (b.quotReqNo() != null) bodyFields.put("quotReqNo", b.quotReqNo());
            if (b.ctrStDt() != null) bodyFields.put("ctrStDt", b.ctrStDt());
            if (b.ctrEdDt() != null) bodyFields.put("ctrEdDt", b.ctrEdDt());
        }
        return inquiryService.contractList(company, bodyFields);
    }

    /** 계약 조회 (Meritz: /trvCtrInq) */
    @PostMapping("/contracts/inquiry")
    public MeritzBridgeApiResponse contractInquiry(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody MeritzTrvCtrInqBody request) {
        Map<String, Object> bodyFields = new LinkedHashMap<>();
        if (request.body() != null) {
            MeritzTrvCtrInqBody.Body b = request.body();
            if (b.polNo() != null) bodyFields.put("polNo", b.polNo());
            if (b.ctrNo() != null) bodyFields.put("ctrNo", b.ctrNo());
        }
        return inquiryService.contractDetail(company, bodyFields);
    }

    /** 가입증명서 출력 */
    @PostMapping("/contracts/certificate")
    public MeritzBridgeApiResponse joinCertificate(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody MeritzCertRequest request) {
        var cmd =
                new CertificateCommand(
                        request.getContractId(), request.getOtptDiv(), request.getOtptTpCd());
        return certificateService.issue(company, cmd);
    }

    /** 여행자보험 접수 */
    @PostMapping("/travel/contract/apply")
    public ContractApplyResponse apply(@RequestBody ContractApplyRequest request) {
        List<ApplyCommand.InsuredPerson> people =
                request.getPeople() == null
                        ? List.of()
                        : request.getPeople().stream()
                                .map(
                                        p ->
                                                new ApplyCommand.InsuredPerson(
                                                        p.getName(),
                                                        p.getGender(),
                                                        p.getResidentNumber(),
                                                        p.getNameEng(),
                                                        p.getPassportNumber(),
                                                        p.getInsureNumber(),
                                                        p.getInsurePremium()))
                                .toList();

        var cmd =
                new ApplyCommand(
                        request.getInsurerId(),
                        request.getPartnerId(),
                        request.getPartnerName(),
                        request.getChannelId(),
                        request.getChannelName(),
                        request.getPlanId(),
                        request.getMeritzQuoteGroupNumber(),
                        request.getMeritzQuoteRequestNumber(),
                        request.getCountryCode(),
                        request.getCountryName(),
                        request.getInsureBeginDate(),
                        request.getInsureEndDate(),
                        request.getContractPeopleName(),
                        request.getContractPeopleResidentNumber(),
                        request.getContractPeopleHp(),
                        request.getContractPeopleMail(),
                        request.getTotalFee(),
                        people,
                        request.isMarketingConsentUsed());

        return ContractApplyResponse.of(applyService.apply(cmd));
    }

    /** 여행자보험 가입(결제) */
    @PostMapping("/travel/contract/completed")
    public ContractCompletedResponse completed(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody ContractCompletedRequest request) {
        var cmd =
                new SubscriptionCommand(
                        request.getContractId(),
                        request.getCardNo(),
                        request.getEfctPrd(),
                        request.getDporNm(),
                        request.getDporCd());
        return ContractCompletedResponse.of(subscriptionService.subscribe(company, cmd));
    }

    /** 여행자보험 가입(결제취소) */
    @PostMapping("/travel/contract/cancel")
    public ContractCancelResponse cancel(
            @RequestParam(defaultValue = "TPA") String company,
            @RequestBody ContractCancelRequest request) {
        var cmd = new CancelCommand(request.getContractId());
        return ContractCancelResponse.of(cancelService.cancel(company, cmd));
    }
}

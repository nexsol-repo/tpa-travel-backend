package com.nexsol.tpa.client.meritz.bridge;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeApiResponse;
import com.nexsol.tpa.client.meritz.dto.quote.MeritzHndyPremCmptBody;

@FeignClient(
        name = "meritz-bridge",
        url = "${meritz.bridge.base-url:https://dev-meritz-bridge.nexsol.ai}")
public interface MeritzBridgeFeignClient {

    // ── 1. 플랜조회 ──

    @PostMapping("/internal/meritz/plan-inquiry")
    MeritzBridgeApiResponse planInquiry(@RequestBody Map<String, Object> body);

    // ── 2. 도시/국가코드 조회 ──

    @PostMapping("/internal/meritz/city-country-code")
    MeritzBridgeApiResponse cityCountryCode(@RequestBody Map<String, Object> body);

    // ── 3. 보험료 산출 ──

    @PostMapping("/internal/meritz/premium-calculate")
    MeritzBridgeApiResponse premiumCalculate(@RequestBody MeritzHndyPremCmptBody body);

    // ── 4. 견적 저장 ──

    @PostMapping("/internal/meritz/estimate-save")
    MeritzBridgeApiResponse estimateSave(@RequestBody Map<String, Object> body);

    // ── 5. 계약 목록 조회 ──

    @PostMapping("/internal/meritz/contract-list")
    MeritzBridgeApiResponse contractList(@RequestBody Map<String, Object> body);

    // ── 6. 가입증명서 ──

    @PostMapping("/internal/meritz/certificate")
    MeritzBridgeApiResponse certificate(@RequestBody Map<String, Object> body);

    // ── 7. 추천플랜 조회 ──

    @PostMapping("/internal/meritz/recommend-plan")
    MeritzBridgeApiResponse recommendPlan(@RequestBody Map<String, Object> body);

    // ── 8. 계약 취소 ──

    @PostMapping("/internal/meritz/contract-cancel")
    MeritzBridgeApiResponse contractCancel(@RequestBody Map<String, Object> body);

    // ── 9. 정산 목록 조회 ──

    @PostMapping("/internal/meritz/settlement-list")
    MeritzBridgeApiResponse settlementList(@RequestBody Map<String, Object> body);

    // ── 10. 고객전환 ──

    @PostMapping("/internal/meritz/customer-convert")
    MeritzBridgeApiResponse customerConvert(@RequestBody Map<String, Object> body);

    // ── 11. 배서보험료 산출 ──

    @PostMapping("/internal/meritz/endorsement-premium")
    MeritzBridgeApiResponse endorsementPremium(@RequestBody Map<String, Object> body);

    // ── 12. 배서체결 저장 ──

    @PostMapping("/internal/meritz/endorsement-save")
    MeritzBridgeApiResponse endorsementSave(@RequestBody Map<String, Object> body);

    // ── 13. 카드승인 ──

    @PostMapping("/internal/meritz/card-approve")
    MeritzBridgeApiResponse cardApprove(@RequestBody Map<String, Object> body);

    // ── 14. 계약 상세 조회 ──

    @PostMapping("/internal/meritz/contract-detail")
    MeritzBridgeApiResponse contractDetail(@RequestBody Map<String, Object> body);

    // ── 15. 카드취소 ──

    @PostMapping("/internal/meritz/card-cancel")
    MeritzBridgeApiResponse cardCancel(@RequestBody Map<String, Object> body);
}

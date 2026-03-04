package com.nexsol.tpa.core.api.service.v1;

import com.nexsol.tpa.client.meritz.bridge.MeritzBridgeClient;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeRequest;
import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeResponse;
import com.nexsol.tpa.core.api.dto.v1.MeritzCertRequest;
import com.nexsol.tpa.core.api.dto.v1.MeritzCommonResponse;
import com.nexsol.tpa.core.api.dto.v1.TravelAlimtalkCompletedCommand;
import com.nexsol.tpa.core.api.dto.v1.contract.*;
import com.nexsol.tpa.core.api.entity.*;
import com.nexsol.tpa.core.api.meritz.config.CompaniesConfigsProperties;
import com.nexsol.tpa.core.api.meritz.dto.contract.MeritzCtrLstInqBody;
import com.nexsol.tpa.core.api.meritz.dto.contract.MeritzTrvCtrInqBody;
import com.nexsol.tpa.core.api.repository.v1.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeritzContractService {

    private static final String EST_SAVE = "/b2b/v1/organ/meritz/estSave";

    // private static final String CRD_CANCEL =
    // "/b2b/v1/organ/meritz/handleOpapiTrvCtrCrdCnc";
    private static final String CTR_CANCEL = "/b2b/v1/organ/meritz/trvChangeCtr";

    private static final String CTR_LST_INQ = "/b2b/v1/organ/meritz/ctrLstInq";

    private static final String TRV_CTR_INQ = "/b2b/v1/organ/meritz/trvCtrInq";

    private static final String JOIN_CERT = "/b2b/v1/organ/meritz/sbcCtfOtpt";

    private final MeritzBridgeClient bridgeClient;

    private final CompaniesConfigsProperties companies;

    private final ObjectMapper objectMapper;

    private final TravelContractRepository contractRepository;

    private final TravelInsurePaymentRepository paymentRepository;

    private final TravelInsurancePlanRepository planRepository;

    private final TravelInsurerRepository insurerRepository;

    private final TravelContractSnapshotRepository snapshotRepository;

    private final AlimtalkService alimtalkService;

    public String contractList(String companyCode, MeritzCtrLstInqBody body) {
        var cfg = resolve(companyCode);

        logJson("[MERITZ][CTR_LST_INQ][REQ]", body);

        MeritzBridgeResponse res = bridgeClient
            .call(new MeritzBridgeRequest(cfg.getCompanyCode(), CTR_LST_INQ, "POST", headers(), body // ✅
                                                                                                     // Object
                                                                                                     // 그대로
            ));

        if (res.getStatus() != 200) {
            throw new IllegalStateException(
                    "Meritz ctrLstInq failed. meritzStatus=" + res.getStatus() + ", body=" + res.getBody());
        }
        return res.getBody();
    }

    public String contractInquiry(String companyCode, MeritzTrvCtrInqBody body) {
        var cfg = resolve(companyCode);

        logJson("[MERITZ][TRV_CTR_INQ][REQ]", body);

        MeritzBridgeResponse res = bridgeClient
            .call(new MeritzBridgeRequest(cfg.getCompanyCode(), TRV_CTR_INQ, "POST", headers(), body // ✅
                                                                                                     // Object
                                                                                                     // 그대로
            ));

        if (res.getStatus() != 200) {
            throw new IllegalStateException(
                    "Meritz trvCtrInq failed. meritzStatus=" + res.getStatus() + ", body=" + res.getBody());
        }
        return res.getBody();
    }

    @Transactional
    public ContractApplyResponse apply(ContractApplyRequest req) {
        // 이 메서드는 외부 메리츠 호출이 아니라 DB 저장만 하니까 변경 없음
        if (req.getPartnerId() == null || req.getChannelId() == null || req.getPlanId() == null) {
            throw new IllegalArgumentException("partnerId/channelId/planId is required");
        }
        if (req.getInsurerId() == null) {
            throw new IllegalArgumentException("insurerCode is required");
        }
        if (req.getPeople() == null || req.getPeople().isEmpty()) {
            throw new IllegalArgumentException("people is required");
        }

        TravelContractEntity c = TravelContractEntity.createPending();

        c.setInsurerId(req.getInsurerId());
        // c.setInsurerName(req.getInsuerName());
        c.setInsurerName("MERITZ");

        c.setPartnerId(req.getPartnerId());
        // c.setPartnerName(req.getPartnerName());
        c.setPartnerName("TPA KOREA");

        c.setChannelId(req.getChannelId());
        // c.setChannelName();
        c.setChannelName("TPA KOREA");

        c.setPlanId(req.getPlanId());

        c.setCountryName(req.getCountryName());
        c.setCountryCode(req.getCountryCode());
        c.setInsureStartDate(req.getInsureBeginDate());
        c.setInsureEndDate(req.getInsureEndDate());

        c.setContractPeopleName(req.getContractPeopleName());
        c.setContractPeopleResidentNumber(req.getContractPeopleResidentNumber());
        c.setContractPeopleHp(req.getContractPeopleHp());
        c.setContractPeopleMail(req.getContractPeopleMail());

        // c.setPolicyNumber(req.getPolicyNumber());
        c.setPolicyNumber(resolve("TPA").getPolNo());

        c.setMeritzQuoteGroupNumber(req.getMeritzQuoteGroupNumber());
        c.setMeritzQuoteRequestNumber(req.getMeritzQuoteRequestNumber());

        c.setTotalPremium(req.getTotalFee());
        c.setInsuredPeopleNumber(req.getPeople().size());
        c.setMarketingConsentUsed(req.isMarketingConsentUsed());

        for (InsurePeopleRequest p : req.getPeople()) {
            if (p.getName() == null || p.getName().isBlank()) {
                throw new IllegalArgumentException("people.name is required");
            }
            TravelInsurePeopleEntity pe = new TravelInsurePeopleEntity();
            pe.setName(p.getName());
            pe.setGender(p.getGender());
            pe.setResidentNumber(p.getResidentNumber());
            pe.setNameEng(p.getNameEng());
            pe.setPassportNumber(p.getPassportNumber());
            pe.setPolicyNumber(p.getInsureNumber());
            pe.setInsurePremium(p.getInsurePremium());
            c.addPerson(pe);
        }

        TravelContractEntity saved = contractRepository.save(c);
        ContractApplyResponse res = new ContractApplyResponse(saved.getId(), saved.getStatus().name());

        String snapshotJson;
        try {
            snapshotJson = objectMapper.writeValueAsString(res);
        }
        catch (Exception e) {
            throw new IllegalStateException("failed to serialize ContractApplyResponse", e);
        }

        TravelContractSnapshotEntity snap = new TravelContractSnapshotEntity();
        snap.setContractId(saved.getId());
        snap.setInsurerId(req.getInsurerId());
        snap.setMethod("apply");
        snap.setSnapshotType("QUOTE");
        snap.setJsonSnapshot(snapshotJson);
        snapshotRepository.save(snap);

        return res;
    }

    @Transactional
    public ContractCompletedResponse completed(String companyCode, ContractCompletedRequest req) {
        CompaniesConfigsProperties.CompanyConfig cfg = resolve(companyCode);

        TravelContractEntity contract = contractRepository.findById(req.getContractId())
            .orElseThrow(() -> new IllegalArgumentException("contract not found: " + req.getContractId()));

        if (contract.getStatus() != TravelContractStatus.PENDING) {
            throw new IllegalStateException("contract status must be PENDING. current=" + contract.getStatus());
        }

        require(req.getCardNo(), "cardNo is required");
        require(req.getEfctPrd(), "efctPrd is required");
        require(req.getDporNm(), "dporNm is required");
        require(req.getDporCd(), "dporCd is required");
        require(contract.getPolicyNumber(), "policyNumber(polNo) is required");

        TravelInsurancePlanEntity plan = planRepository.findById(contract.getPlanId())
            .orElseThrow(() -> new IllegalStateException("plan not found. planId=" + contract.getPlanId()));

        TravelInsurerEntity insurer = insurerRepository.findById(plan.getInsurerId())
            .orElseThrow(() -> new IllegalStateException("insurer not found. insurerId=" + plan.getInsurerId()));

        String sbcpDt = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());
        body.put("bizpeNo", cfg.getBizpeNo());
        body.put("polNo", contract.getPolicyNumber());

        body.put("pdCd", plan.getProductCode());
        body.put("untPdCd", plan.getUnitProductCode());
        body.put("sbcpDt", sbcpDt);

        body.put("insBgnDt",
                contract.getInsureStartDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")));
        body.put("insEdDt",
                contract.getInsureEndDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")));
        log.info("[MERITZ][EST_SAVE] insureStartDate={}, insureEndDate={}", contract.getInsureStartDate(), contract.getInsureEndDate());
        body.put("trvArCd", contract.getCountryCode());

        body.put("inspeCnt", contract.getPeople().size());

        List<Map<String, Object>> insuredVos = contract.getPeople().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("inspeRsidNo", null);
            m.put("inspeBdt", extractBirthYmd(p.getResidentNumber()));
            m.put("gndrCd", normalizeGenderToMeritz(p.getGender(), p.getResidentNumber()));
            m.put("inspeNm", p.getName());
            m.put("engInspeNm", p.getNameEng());
            m.put("planGrpCd", plan.getPlanGroupCode());
            m.put("planCd", plan.getPlanCode());
            return m;
        }).toList();
        body.put("opapiTrvPremCmptInspeInfCbcVo", insuredVos);

        Map<String, Object> card = new LinkedHashMap<>();
        card.put("crdNo", req.getCardNo());
        card.put("efctPrd", req.getEfctPrd());
        card.put("dporNm", req.getDporNm());
        card.put("dporCd", req.getDporCd());
        body.put("ctrTrsInfBcVo", card);

        logJson("[MERITZ][EST_SAVE][REQ]", body);

        MeritzBridgeResponse res = bridgeClient
            .call(new MeritzBridgeRequest(cfg.getCompanyCode(), EST_SAVE, "POST", headers(), body // ✅
                                                                                                  // Object
                                                                                                  // 그대로
            ));

        if (res.getStatus() != 200) {
            throw new IllegalStateException(
                    "Meritz estSave failed. status=" + res.getStatus() + ", body=" + res.getBody());
        }

        MeritzCommonResponse meritz = readMeritz(res.getBody());
        if (!"00001".equals(meritz.getErrCd())) {
            // throw new IllegalStateException("Meritz estSave errCd=" + meritz.getErrCd()
            // + ", errMsg=" + meritz.getErrMsg());

            TravelContractSnapshotEntity snapshot = new TravelContractSnapshotEntity();
            snapshot.setContractId(contract.getId());
            snapshot.setInsurerId(contract.getInsurerId());
            snapshot.setMethod("api");
            snapshot.setSnapshotType("PAYMENT_FAIL"); // 타입 구분 추천
            snapshot.setJsonSnapshot(res.getBody());
            snapshotRepository.save(snapshot);

            return new ContractCompletedResponse(meritz.getErrCd(), meritz.getErrMsg());
        }

        if (meritz.getTtPrem() != null)
            contract.setTotalPremium(meritz.getTtPrem());
        if (meritz.getPolNo() != null && !meritz.getPolNo().isBlank())
            contract.setPolicyNumber(meritz.getPolNo());
        if (meritz.getQuotGrpNo() != null && !meritz.getQuotGrpNo().isBlank())
            contract.setMeritzQuoteGroupNumber(meritz.getQuotGrpNo());
        if (meritz.getQuotReqNo() != null && !meritz.getQuotReqNo().isBlank())
            contract.setMeritzQuoteRequestNumber(meritz.getQuotReqNo());

        try {
            // joinCertificate에서 이미 body 구성/호출/스냅샷 저장을 하고 있으니
            // 여기서는 "링크만" 리턴하도록 메서드 하나 더 만드는 걸 추천
            String policyLink = joinCertificateLink(companyCode, contract.getId(), "A", "V"); // 국문
                                                                                              // +
                                                                                              // viewer

            if (policyLink != null && !policyLink.isBlank()) {
                contract.setPolicyLink(policyLink);
            }
        }
        catch (Exception e) {
            // 증명서 링크 실패해도 결제완료 자체는 유지 (운영적으로 더 안전)
            // 로그만 남겨두자
            log.warn("joinCertificate failed. contractId={}, msg={}", contract.getId(), e.getMessage(), e);
        }

        contract.markCompleted();
        contractRepository.save(contract);

        if (paymentRepository.existsByContractId(contract.getId())) {
            throw new IllegalStateException("payment already exists. contractId=" + contract.getId());
        }

        TravelInsurePaymentEntity pay = new TravelInsurePaymentEntity();
        pay.setContractId(contract.getId());
        pay.setPaymentMethod(TravelPaymentMethod.CARD);
        pay.setPaidAmount(contract.getTotalPremium());
        pay.setStatus(TravelPaymentStatus.COMPLETED);
        pay.setPaymentDate(java.time.LocalDateTime.now());
        paymentRepository.save(pay);

        TravelContractSnapshotEntity snapshot = new TravelContractSnapshotEntity();
        snapshot.setContractId(contract.getId());
        snapshot.setInsurerId(contract.getInsurerId());
        snapshot.setMethod("api");
        snapshot.setSnapshotType("PAYMENT");
        snapshot.setJsonSnapshot(res.getBody());
        snapshotRepository.save(snapshot);

        // 알림톡 발송
        try {
            alimtalkService.sendTravelContractCompleted(TravelAlimtalkCompletedCommand.builder()
                .receiverHp(contract.getContractPeopleHp())
                .receiverName(contract.getContractPeopleName())
                .productName("여행자보험") // 너희 표기
                .policyNumber(contract.getPolicyNumber())
                .certificateUrl(contract.getPolicyLink())
                .termsUrl("https://filer.bucket.nexsol.ai/buckets/tpa-travel-dev/insurance/term.pdf")
                .build());
        }
        catch (Exception ignore) {
            // send 내부에서 이미 catch하므로 여기까지 안 와도 됨 (안전빵)
        }

        return new ContractCompletedResponse(new ContractCompletedResponse.Contract(contract.getId(),
                contract.getPartnerId(), contract.getChannelId(), contract.getPlanId(), contract.getPolicyNumber(),
                contract.getMeritzQuoteGroupNumber(), contract.getMeritzQuoteRequestNumber(), contract.getCountryName(),
                contract.getCountryCode(), contract.getInsuredPeopleNumber(), contract.getTotalPremium(),
                contract.getStatus().name(), contract.getInsureStartDate(), contract.getInsureEndDate(),
                contract.getContractPeopleName(), contract.getContractPeopleHp(), contract.getContractPeopleMail()),
                new ContractCompletedResponse.Insurer(insurer.getId(), insurer.getInsurerName(),
                        insurer.getInsurerCode()),
                new ContractCompletedResponse.Plan(plan.getId(), plan.getInsuranceProductName(), plan.getPlanName(),
                        plan.getProductCode(), plan.getUnitProductCode(), plan.getPlanGroupCode(), plan.getPlanCode()));
    }

    public String joinCertificateLink(String companyCode, Long contractId, String otptDiv, String otptTpCd) {
        var cfg = resolve(companyCode);

        TravelContractEntity contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new IllegalArgumentException("contract not found: " + contractId));

        TravelInsurancePlanEntity plan = planRepository.findById(contract.getPlanId())
            .orElseThrow(() -> new IllegalStateException("plan not found. planId=" + contract.getPlanId()));

        // 기본값
        String div = (otptDiv == null || otptDiv.isBlank()) ? "A" : otptDiv.trim();
        String tp = (otptTpCd == null || otptTpCd.isBlank()) ? "V" : otptTpCd.trim();

        // 필수값 검증
        require(contract.getPolicyNumber(), "policyNumber(polNo) is required");
        require(contract.getMeritzQuoteGroupNumber(), "quotGrpNo is required");
        require(contract.getMeritzQuoteRequestNumber(), "quotReqNo is required");
        require(plan.getProductCode(), "pdCd(productCode) is required");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());
        body.put("bizpeNo", cfg.getBizpeNo());
        body.put("polNo", contract.getPolicyNumber());
        body.put("pdCd", plan.getProductCode());
        body.put("quotGrpNo", contract.getMeritzQuoteGroupNumber());
        body.put("quotReqNo", contract.getMeritzQuoteRequestNumber());
        body.put("otptDiv", div);
        body.put("otptTpCd", tp);

        logJson("[MERITZ][JOIN_CERT][REQ]", body);

        MeritzBridgeResponse res = bridgeClient
            .call(new MeritzBridgeRequest(cfg.getCompanyCode(), JOIN_CERT, "POST", headers(), body));

        if (res.getStatus() != 200) {
            throw new IllegalStateException(
                    "Meritz sbcCtfOtpt failed. status=" + res.getStatus() + ", body=" + res.getBody());
        }

        MeritzCommonResponse meritz = readMeritz(res.getBody());
        if (!isMeritzSuccess(meritz.getErrCd())) {
            throw new IllegalStateException(
                    "Meritz joinCertificate errCd=" + meritz.getErrCd() + ", errMsg=" + meritz.getErrMsg());
        }

        // 스냅샷 저장(유지)
        TravelContractSnapshotEntity snapshot = new TravelContractSnapshotEntity();
        snapshot.setContractId(contract.getId());
        snapshot.setInsurerId(contract.getInsurerId());
        snapshot.setMethod("api");
        snapshot.setSnapshotType("CERTIFICATE");
        snapshot.setJsonSnapshot(res.getBody());
        snapshotRepository.save(snapshot);

        // 링크만 리턴
        return meritz.getRltLinkUrl();
    }

    @Transactional
    public ContractCancelResponse cancel(String companyCode, ContractCancelRequest req) {
        CompaniesConfigsProperties.CompanyConfig cfg = resolve(companyCode);

        TravelContractEntity contract = contractRepository.findById(req.getContractId())
            .orElseThrow(() -> new IllegalArgumentException("contract not found: " + req.getContractId()));

        TravelInsurePaymentEntity payment = paymentRepository.findByContractId(contract.getId())
            .orElseThrow(() -> new IllegalStateException("payment not found. contractId=" + contract.getId()));

        TravelInsurancePlanEntity plan = planRepository.findById(contract.getPlanId())
            .orElseThrow(() -> new IllegalStateException("plan not found. planId=" + contract.getPlanId()));

        TravelInsurerEntity insurer = insurerRepository.findById(plan.getInsurerId())
            .orElseThrow(() -> new IllegalStateException("insurer not found. insurerId=" + plan.getInsurerId()));

        if (payment.getStatus() == TravelPaymentStatus.CANCELED) {
            return buildCancelResponse(contract, payment, plan, insurer);
        }

        if (payment.getStatus() != TravelPaymentStatus.COMPLETED) {
            throw new IllegalStateException("payment status must be COMPLETED. current=" + payment.getStatus());
        }

        require(contract.getPolicyNumber(), "policyNumber(polNo) is required");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());
        body.put("bizpeNo", cfg.getBizpeNo());
        body.put("polNo", contract.getPolicyNumber());

        if (contract.getMeritzQuoteGroupNumber() != null && !contract.getMeritzQuoteGroupNumber().isBlank()) {
            body.put("quotGrpNo", contract.getMeritzQuoteGroupNumber());
        }
        if (contract.getMeritzQuoteRequestNumber() != null && !contract.getMeritzQuoteRequestNumber().isBlank()) {
            body.put("quotReqNo", contract.getMeritzQuoteRequestNumber());
        }

        // body.put("paidAmt", payment.getPaidAmount());

        logJson("[MERITZ][CRD_CANCEL][REQ]", body);

        MeritzBridgeResponse res = bridgeClient
            .call(new MeritzBridgeRequest(cfg.getCompanyCode(), CTR_CANCEL, "POST", headers(), body // ✅
                                                                                                    // Object
                                                                                                    // 그대로
            ));

        if (res.getStatus() != 200) {
            throw new IllegalStateException(
                    "Meritz cancel failed. status=" + res.getStatus() + ", body=" + res.getBody());
        }

        MeritzCommonResponse meritz = readMeritz(res.getBody());
        if (!"00001".equals(meritz.getErrCd())) {
            throw new IllegalStateException(
                    "Meritz cancel errCd=" + meritz.getErrCd() + ", errMsg=" + meritz.getErrMsg());
        }

        payment.setStatus(TravelPaymentStatus.CANCELED);
        payment.setCancelDate(LocalDateTime.now());
        paymentRepository.save(payment);

        TravelContractSnapshotEntity snapshot = new TravelContractSnapshotEntity();
        snapshot.setContractId(contract.getId());
        snapshot.setInsurerId(contract.getInsurerId());
        snapshot.setMethod("api");
        snapshot.setSnapshotType("CANCEL");
        snapshot.setJsonSnapshot(res.getBody());
        snapshotRepository.save(snapshot);

        return buildCancelResponse(contract, payment, plan, insurer);
    }

    private ContractCancelResponse buildCancelResponse(TravelContractEntity contract, TravelInsurePaymentEntity payment,
            TravelInsurancePlanEntity plan, TravelInsurerEntity insurer) {
        BigDecimal refundAmount = contract.getTotalPremium();
        return new ContractCancelResponse(new ContractCancelResponse.Contract(contract.getId(),
                contract.getStatus().name(), contract.getPolicyNumber(), contract.getMeritzQuoteGroupNumber(),
                contract.getMeritzQuoteRequestNumber(), contract.getCountryName(), contract.getCountryCode(),
                contract.getInsuredPeopleNumber(), contract.getTotalPremium(), contract.getInsureStartDate(),
                contract.getInsureEndDate(),
                new ContractCancelResponse.Insurer(insurer.getId(), insurer.getInsurerCode(), insurer.getInsurerName()),
                new ContractCancelResponse.Plan(plan.getId(), plan.getInsuranceProductName(), plan.getPlanName(),
                        plan.getProductCode(), plan.getUnitProductCode(), plan.getPlanGroupCode(), plan.getPlanCode()),
                new ContractCancelResponse.Payment(payment.getId(), payment.getPaymentMethod().name(),
                        payment.getStatus().name(), payment.getPaidAmount(), payment.getPaymentDate(),
                        payment.getCancelDate(), null),
                refundAmount));
    }

    /** ======================= 공통 ======================= */

    private Map<String, String> headers() {
        return Map.of("Content-Type", "application/json; charset=UTF-8");
    }

    private void logJson(String prefix, Object body) {
        try {
            log.info("{} body={}", prefix, objectMapper.writeValueAsString(body));
        }
        catch (Exception e) {
            log.info("{} body=(json serialize fail) {}", prefix, body);
        }
    }

    private MeritzCommonResponse readMeritz(String json) {
        try {
            return objectMapper.readValue(json, MeritzCommonResponse.class);
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to parse meritz response JSON: " + json, e);
        }
    }

    private CompaniesConfigsProperties.CompanyConfig resolve(String companyCode) {
        if (companyCode == null || companyCode.isBlank())
            companyCode = "TPA";
        if ("TPA".equalsIgnoreCase(companyCode))
            return companies.getTpa();
        if ("INSBOON".equalsIgnoreCase(companyCode))
            return companies.getInsboon();
        throw new IllegalArgumentException("Unknown companyCode: " + companyCode);
    }

    private static void require(String v, String msg) {
        if (v == null || v.isBlank())
            throw new IllegalArgumentException(msg);
    }

    private static String extractBirthYmd(String rrn) {
        if (rrn == null)
            throw new IllegalStateException("residentNumber is required to build inspeBdt");
        String digits = rrn.replaceAll("[^0-9]", "");
        if (digits.length() < 7)
            throw new IllegalStateException("invalid residentNumber: " + rrn);

        String yymmdd = digits.substring(0, 6);
        char s = digits.charAt(6);

        String century;
        if (s == '1' || s == '2' || s == '5' || s == '6')
            century = "19";
        else if (s == '3' || s == '4' || s == '7' || s == '8')
            century = "20";
        else
            century = "19";

        return century + yymmdd;
    }

    private static String normalizeGenderToMeritz(String gender, String rrn) {
        if (gender != null) {
            String g = gender.trim().toUpperCase();
            if ("1".equals(g) || "2".equals(g))
                return g;
            if ("M".equals(g) || "MALE".equals(g) || "남".equals(g))
                return "1";
            if ("F".equals(g) || "FEMALE".equals(g) || "여".equals(g))
                return "2";
        }

        String digits = rrn == null ? "" : rrn.replaceAll("[^0-9]", "");
        if (digits.length() >= 7) {
            char s = digits.charAt(6);
            if (s == '1' || s == '3' || s == '5' || s == '7')
                return "1";
            if (s == '2' || s == '4' || s == '6' || s == '8')
                return "2";
        }

        throw new IllegalStateException("cannot determine gender. gender=" + gender + ", rrn=" + rrn);
    }

    public String joinCertificate(String companyCode, MeritzCertRequest req) {
        var cfg = resolve(companyCode);

        if (req == null || req.getContractId() == null) {
            throw new IllegalArgumentException("contractId is required");
        }

        // 출력구분/유형 기본값
        String otptDiv = (req.getOtptDiv() == null || req.getOtptDiv().isBlank()) ? "A" : req.getOtptDiv().trim();
        String otptTpCd = (req.getOtptTpCd() == null || req.getOtptTpCd().isBlank()) ? "V" : req.getOtptTpCd().trim();

        TravelContractEntity contract = contractRepository.findById(req.getContractId())
            .orElseThrow(() -> new IllegalArgumentException("contract not found: " + req.getContractId()));

        TravelInsurancePlanEntity plan = planRepository.findById(contract.getPlanId())
            .orElseThrow(() -> new IllegalStateException("plan not found. planId=" + contract.getPlanId()));

        // 필수값 검증
        require(contract.getPolicyNumber(), "policyNumber(polNo) is required");
        require(contract.getMeritzQuoteGroupNumber(), "quotGrpNo is required");
        require(contract.getMeritzQuoteRequestNumber(), "quotReqNo is required");
        require(plan.getProductCode(), "pdCd(productCode) is required");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("gnrAflcoCd", cfg.getGnrAflcoCd());
        body.put("aflcoDivCd", cfg.getAflcoDivCd());
        body.put("bizpeNo", cfg.getBizpeNo());
        body.put("polNo", contract.getPolicyNumber());
        body.put("pdCd", plan.getProductCode());
        body.put("quotGrpNo", contract.getMeritzQuoteGroupNumber());
        body.put("quotReqNo", contract.getMeritzQuoteRequestNumber());
        body.put("otptDiv", otptDiv);
        body.put("otptTpCd", otptTpCd);

        logJson("[MERITZ][JOIN_CERT][REQ]", body);

        MeritzBridgeResponse res = bridgeClient
            .call(new MeritzBridgeRequest(cfg.getCompanyCode(), JOIN_CERT, "POST", headers(), body));

        if (res.getStatus() != 200) {
            throw new IllegalStateException(
                    "Meritz sbcCtfOtpt failed. status=" + res.getStatus() + ", body=" + res.getBody());
        }

        // 공통 에러코드 체크(성공코드 00000/00001 둘다 허용 추천)
        MeritzCommonResponse meritz = readMeritz(res.getBody());
        if (!isMeritzSuccess(meritz.getErrCd())) {
            throw new IllegalStateException(
                    "Meritz joinCertificate errCd=" + meritz.getErrCd() + ", errMsg=" + meritz.getErrMsg());
        }

        // 스냅샷 저장(원하면 유지)
        TravelContractSnapshotEntity snapshot = new TravelContractSnapshotEntity();
        snapshot.setContractId(contract.getId());
        snapshot.setInsurerId(contract.getInsurerId());
        snapshot.setMethod("api");
        snapshot.setSnapshotType("CERTIFICATE");
        snapshot.setJsonSnapshot(res.getBody());
        snapshotRepository.save(snapshot);

        return res.getBody();
    }

    private static boolean isMeritzSuccess(String errCd) {
        if (errCd == null)
            return false;
        return "00000".equals(errCd) || "00001".equals(errCd);
    }

}

package com.nexsol.tpa.core.api.service;

import com.nexsol.tpa.core.api.dto.v1.AuthCertResultResponse;
import com.nexsol.tpa.core.api.entity.TpaAuthCertLogEntity;
import com.nexsol.tpa.core.api.entity.TpaAuthCertResultEntity;
import com.nexsol.tpa.core.api.entity.TravelContractEntity;
import com.nexsol.tpa.core.api.entity.TravelContractSnapshotEntity;
import com.nexsol.tpa.core.api.repository.v1.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthCertPersistenceService {

    private final TpaAuthCertLogRepository logRepo;
    private final TpaAuthCertResultRepository resultRepo;
    private final TravelContractRepository contractRepo;
    private final TravelContractSnapshotRepository snapshotRepo;

    /**
     * tpa_auth_cert_log : impUid 기준 멱등 upsert
     */
    @Transactional
    public Long createOrUpdateLog(
            Long contractId,
            String bizNum,
            String impUid,
            String requestId,
            String pathRoot,
            String moid,
            String pg,
            String provider,
            String userAgent,
            String clientIp,
            String referer,
            String rawReqJson
    ) {

        TpaAuthCertLogEntity log = logRepo.findByImpUid(impUid).orElseGet(TpaAuthCertLogEntity::new);

        // log.setContractId(contractId); // 컬럼 추가하면 활성화
        log.setBizNum(bizNum);
        log.setImpUid(impUid);
        log.setRequestId(requestId);
        log.setPathRoot(pathRoot);
        log.setMoid(moid);
        log.setPg(pg);
        log.setProvider(provider == null ? "DANAL_PASS" : provider);
        log.setUserAgent(userAgent);
        log.setClientIp(clientIp);
        log.setReferer(referer);
        // log.setRawReqJson(rawReqJson);

        return logRepo.save(log).getId();
    }

    /**
     * result upsert + contract update + snapshot upsert
     */
    @Transactional
    public AuthCertResultResponse saveResultAndUpdateContract(
            Long contractId,
            String provider,
            String moid,
            String impUid,
            String requestId,
            String uniqueKey,
            String resultStatus,
            String resultCode,
            String resultMsg,
            String certName,
            String certBirthday,
            String certGender,
            String certPhone,
            boolean matched,
            String matchFailReason,
            String rawResJson
    ) {

        TravelContractEntity contract = contractRepo.findById(contractId).orElseThrow(() -> new EntityNotFoundException("계약 없음. contractId=" + contractId));

        // 계약 업데이트
        contract.setAuthProvider(provider);
        contract.setAuthImpUid(impUid);
        contract.setAuthRequestId(requestId);
        contract.setAuthUniqueKey(uniqueKey);
        contract.setAuthStatus("SUCCESS".equalsIgnoreCase(resultStatus) ? "SUCCESS" : "FAIL");
        contract.setAuthDate(LocalDateTime.now());
        contractRepo.save(contract);

        // logId 연결 (impUid 기준)
        Long logId = logRepo.findByImpUid(impUid)
                .map(TpaAuthCertLogEntity::getId)
                .orElse(null);

        // result 멱등 upsert
        TpaAuthCertResultEntity result = resultRepo.findByImpUid(impUid)
                .orElseGet(TpaAuthCertResultEntity::new);

        // result.setContractId(contractId); // 컬럼 추가하면 활성화
        result.setLogId(logId);
        result.setImpUid(impUid);
        result.setRequestId(requestId);
        result.setMoid(moid);
        result.setUniqueKey(uniqueKey);
        result.setResultStatus(resultStatus);
        result.setResultCode(resultCode);
        result.setResultMsg(resultMsg);

        result.setCertName(certName);
        result.setCertBirthday(normalizeBirthday(certBirthday));
        result.setCertGender(certGender);
        result.setCertPhone(certPhone);

        result.setMatchedYn(matched ? "Y" : "N");
        result.setMatchFailReason(matched ? null : matchFailReason);
        result.setRawResJson(rawResJson);

        resultRepo.save(result);

        // AUTH snapshot upsert
        if (contract.getInsurerId() != null && rawResJson != null && !rawResJson.isBlank()) {
            upsertAuthSnapshot(contractId, contract.getInsurerId(), rawResJson);
        }

        // response
        AuthCertResultResponse res = new AuthCertResultResponse();
        res.setMoid(moid);
        res.setImpUid(impUid);
        res.setRequestId(requestId);
        res.setUniqueKey(uniqueKey);
        res.setResultStatus(resultStatus);
        res.setResultCode(resultCode);
        res.setResultMsg(resultMsg);
        res.setCertName(certName);
        res.setCertBirthday(certBirthday);
        res.setCertGender(certGender);
        res.setCertPhone(certPhone);
        res.setMatchedYn(matched ? "Y" : "N");
        res.setMatchFailReason(matched ? null : matchFailReason);
        return res;
    }

    private String normalizeBirthday(String raw) {
        if (raw == null) return null;
        // 숫자만 남김
        String digits = raw.replaceAll("\\D", "");

        // 8자리(YYYYMMDD)면 그대로
        if (digits.length() >= 8) {
            return digits.substring(0, 8);
        }

        // 혹시 6자리(YYMMDD)가 오면 정책 결정 필요:
        // 여기선 그대로 반환(또는 null 처리)
        return digits;
    }

    private void upsertAuthSnapshot(Long contractId, Long insurerId, String jsonSnapshot) {
        final String SNAPSHOT_TYPE = "AUTH";
        final String METHOD = "API";

        TravelContractSnapshotEntity snap = snapshotRepo
                .findByContractIdAndSnapshotType(contractId, SNAPSHOT_TYPE)
                .orElseGet(() -> {
                    TravelContractSnapshotEntity s = new TravelContractSnapshotEntity();
                    s.setContractId(contractId);
                    s.setInsurerId(insurerId);
                    s.setMethod(METHOD);
                    s.setSnapshotType(SNAPSHOT_TYPE);
                    return s;
                });

        snap.setJsonSnapshot(jsonSnapshot);
        snapshotRepo.save(snap);
    }

    @Transactional
    public AuthCertResultResponse saveHistoryResult(
            String provider,
            String moid,
            String impUid,
            String requestId,
            String uniqueKey,
            String resultStatus,
            String resultCode,
            String resultMsg,
            String certName,
            String certBirthday,
            String certGender,
            String certPhone,
            String rawResJson
    ) {
        // logId 연결 (impUid 기준)
        Long logId = logRepo.findByImpUid(impUid)
                .map(TpaAuthCertLogEntity::getId)
                .orElse(null);

        // result 멱등 upsert
        TpaAuthCertResultEntity result = resultRepo.findByImpUid(impUid)
                .orElseGet(TpaAuthCertResultEntity::new);

        result.setLogId(logId);
        result.setImpUid(impUid);
        result.setRequestId(requestId);
        result.setMoid(moid);
        result.setUniqueKey(uniqueKey);
        result.setResultStatus(resultStatus);
        result.setResultCode(resultCode);
        result.setResultMsg(resultMsg);

        result.setCertName(certName);
        result.setCertBirthday(normalizeBirthday(certBirthday));
        result.setCertGender(certGender);
        result.setCertPhone(certPhone);

        // 가입조회는 계약 매칭 개념 없음 → 기본값 처리
        result.setMatchedYn("Y");
        result.setMatchFailReason(null);

        result.setRawResJson(rawResJson);

        resultRepo.save(result);

        // response
        AuthCertResultResponse res = new AuthCertResultResponse();
        res.setMoid(moid);
        res.setImpUid(impUid);
        res.setRequestId(requestId);
        res.setUniqueKey(uniqueKey);
        res.setResultStatus(resultStatus);
        res.setResultCode(resultCode);
        res.setResultMsg(resultMsg);
        res.setCertName(certName);
        res.setCertBirthday(certBirthday);
        res.setCertGender(certGender);
        res.setCertPhone(certPhone);
        res.setMatchedYn("Y");
        res.setMatchFailReason(null);
        return res;
    }

}

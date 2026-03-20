package com.nexsol.tpa.core.domain.auth;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;
import com.nexsol.tpa.storage.db.core.entity.TpaAuthCertLogEntity;
import com.nexsol.tpa.storage.db.core.entity.TpaAuthCertResultEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelContractSnapshotEntity;
import com.nexsol.tpa.storage.db.core.repository.TpaAuthCertLogRepository;
import com.nexsol.tpa.storage.db.core.repository.TpaAuthCertResultRepository;
import com.nexsol.tpa.storage.db.core.repository.TravelContractRepository;
import com.nexsol.tpa.storage.db.core.repository.TravelContractSnapshotRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthCertWriter {

    private final TpaAuthCertLogRepository logRepository;
    private final TpaAuthCertResultRepository resultRepository;
    private final TravelContractRepository contractRepository;
    private final TravelContractSnapshotRepository snapshotRepository;

    @Transactional
    public Long createOrUpdateLog(AuthCertLogInfo info) {
        TpaAuthCertLogEntity log =
                logRepository
                        .findByImpUid(info.impUid())
                        .orElseGet(TpaAuthCertLogEntity::createEmpty);
        log.update(
                info.bizNum(),
                info.impUid(),
                info.requestId(),
                info.pathRoot(),
                info.moid(),
                info.pg(),
                info.provider(),
                info.userAgent(),
                info.clientIp(),
                info.referer());

        return logRepository.save(log).getId();
    }

    @Transactional
    public AuthCertResult saveResultAndUpdateContract(Long contractId, AuthCertResultInfo info) {
        TravelContractEntity contract =
                contractRepository
                        .findById(contractId)
                        .orElseThrow(
                                () ->
                                        new CoreApiException(
                                                CoreApiErrorType.AUTH_CONTRACT_NOT_FOUND,
                                                "contractId=" + contractId));

        contract.updateAuth(
                info.provider(),
                info.impUid(),
                info.requestId(),
                info.uniqueKey(),
                "SUCCESS".equalsIgnoreCase(info.resultStatus()) ? "SUCCESS" : "FAIL");
        contractRepository.save(contract);

        Long logId =
                logRepository
                        .findByImpUid(info.impUid())
                        .map(TpaAuthCertLogEntity::getId)
                        .orElse(null);

        TpaAuthCertResultEntity result =
                resultRepository
                        .findByImpUid(info.impUid())
                        .orElseGet(TpaAuthCertResultEntity::createEmpty);
        result.update(
                logId,
                info.impUid(),
                info.requestId(),
                info.moid(),
                info.uniqueKey(),
                info.resultStatus(),
                info.resultCode(),
                info.resultMsg(),
                info.certName(),
                normalizeBirthday(info.certBirthday()),
                info.certGender(),
                info.certPhone(),
                info.matched() ? "Y" : "N",
                info.matched() ? null : info.matchFailReason(),
                info.rawResJson());

        resultRepository.save(result);

        if (contract.getInsurerId() != null
                && info.rawResJson() != null
                && !info.rawResJson().isBlank()) {
            upsertAuthSnapshot(contractId, contract.getInsurerId(), info.rawResJson());
        }

        return toResult(info);
    }

    @Transactional
    public AuthCertResult saveHistoryResult(AuthCertResultInfo info) {
        Long logId =
                logRepository
                        .findByImpUid(info.impUid())
                        .map(TpaAuthCertLogEntity::getId)
                        .orElse(null);

        TpaAuthCertResultEntity result =
                resultRepository
                        .findByImpUid(info.impUid())
                        .orElseGet(TpaAuthCertResultEntity::createEmpty);
        result.update(
                logId,
                info.impUid(),
                info.requestId(),
                info.moid(),
                info.uniqueKey(),
                info.resultStatus(),
                info.resultCode(),
                info.resultMsg(),
                info.certName(),
                normalizeBirthday(info.certBirthday()),
                info.certGender(),
                info.certPhone(),
                "Y",
                null,
                info.rawResJson());

        resultRepository.save(result);

        return toResult(info);
    }

    private AuthCertResult toResult(AuthCertResultInfo info) {
        return new AuthCertResult(
                info.moid(),
                info.impUid(),
                info.requestId(),
                null,
                info.uniqueKey(),
                info.resultStatus(),
                info.resultCode(),
                info.resultMsg(),
                info.certName(),
                info.certBirthday(),
                info.certGender(),
                info.certPhone(),
                info.matched() ? "Y" : "N",
                info.matched() ? null : info.matchFailReason());
    }

    private String normalizeBirthday(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() >= 8) {
            return digits.substring(0, 8);
        }
        return digits;
    }

    private void upsertAuthSnapshot(Long contractId, Long insurerId, String jsonSnapshot) {
        final String snapshotType = "AUTH";
        final String method = "API";

        TravelContractSnapshotEntity snap =
                snapshotRepository
                        .findByContractIdAndSnapshotType(contractId, snapshotType)
                        .orElseGet(
                                () ->
                                        TravelContractSnapshotEntity.builder()
                                                .contractId(contractId)
                                                .insurerId(insurerId)
                                                .method(method)
                                                .snapshotType(snapshotType)
                                                .build());

        snap.updateSnapshot(jsonSnapshot);
        snapshotRepository.save(snap);
    }
}

package com.nexsol.tpa.core.domain.auth;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.contract.ContractUpdater;
import com.nexsol.tpa.core.domain.contract.ContractWriter;
import com.nexsol.tpa.core.domain.repository.AuthCertLogRepository;
import com.nexsol.tpa.core.domain.repository.AuthCertResultRepository;
import com.nexsol.tpa.core.domain.repository.ContractRepository;
import com.nexsol.tpa.core.domain.repository.ContractSnapshotRepository;
import com.nexsol.tpa.core.domain.snapshot.ContractSnapshot;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthCertWriter {

    private final AuthCertLogRepository logRepository;
    private final AuthCertResultRepository resultRepository;
    private final ContractRepository contractRepository;
    private final ContractSnapshotRepository snapshotRepository;
    private final ContractUpdater contractUpdater;
    private final ContractWriter contractWriter;

    public Long createOrUpdateLog(AuthCertLogInfo info) {
        AuthCertLogInfo existing = logRepository.findByImpUid(info.impUid()).orElse(null);

        AuthCertLogInfo logInfo =
                AuthCertLogInfo.builder()
                        .id(existing != null ? existing.id() : null)
                        .contractId(info.contractId())
                        .bizNum(info.bizNum())
                        .impUid(info.impUid())
                        .requestId(info.requestId())
                        .pathRoot(info.pathRoot())
                        .moid(info.moid())
                        .pg(info.pg())
                        .provider(info.provider())
                        .userAgent(info.userAgent())
                        .clientIp(info.clientIp())
                        .referer(info.referer())
                        .build();

        return logRepository.save(logInfo).id();
    }

    public AuthCertResult saveResultAndUpdateContract(Long contractId, AuthCertResultInfo info) {
        ContractInfo contract =
                contractRepository
                        .findById(contractId)
                        .orElseThrow(
                                () ->
                                        new CoreException(
                                                CoreErrorType.AUTH_CONTRACT_NOT_FOUND,
                                                "contractId=" + contractId));

        ContractInfo updatedContract =
                contractUpdater.updateAuth(
                        contract,
                        info.provider(),
                        info.impUid(),
                        info.requestId(),
                        info.uniqueKey(),
                        "SUCCESS".equalsIgnoreCase(info.resultStatus()) ? "SUCCESS" : "FAIL");
        contractWriter.writerContract(updatedContract);

        Long logId =
                logRepository.findByImpUid(info.impUid()).map(AuthCertLogInfo::id).orElse(null);

        AuthCertResultInfo existingResult =
                resultRepository.findByImpUid(info.impUid()).orElse(null);

        AuthCertResultInfo resultInfo =
                AuthCertResultInfo.builder()
                        .id(existingResult != null ? existingResult.id() : null)
                        .logId(logId)
                        .impUid(info.impUid())
                        .requestId(info.requestId())
                        .moid(info.moid())
                        .uniqueKey(info.uniqueKey())
                        .resultStatus(info.resultStatus())
                        .resultCode(info.resultCode())
                        .resultMsg(info.resultMsg())
                        .certName(info.certName())
                        .certBirthday(normalizeBirthday(info.certBirthday()))
                        .certGender(info.certGender())
                        .certPhone(info.certPhone())
                        .matchedYn(info.matched() ? "Y" : "N")
                        .matchFailReason(info.matched() ? null : info.matchFailReason())
                        .rawResJson(info.rawResJson())
                        .build();

        resultRepository.save(resultInfo);

        if (contract.insurerId() != null
                && info.rawResJson() != null
                && !info.rawResJson().isBlank()) {
            upsertAuthSnapshot(contractId, contract.insurerId(), info.rawResJson());
        }

        return toResult(info);
    }

    public AuthCertResult saveHistoryResult(AuthCertResultInfo info) {
        Long logId =
                logRepository.findByImpUid(info.impUid()).map(AuthCertLogInfo::id).orElse(null);

        AuthCertResultInfo existingResult =
                resultRepository.findByImpUid(info.impUid()).orElse(null);

        AuthCertResultInfo resultInfo =
                AuthCertResultInfo.builder()
                        .id(existingResult != null ? existingResult.id() : null)
                        .logId(logId)
                        .impUid(info.impUid())
                        .requestId(info.requestId())
                        .moid(info.moid())
                        .uniqueKey(info.uniqueKey())
                        .resultStatus(info.resultStatus())
                        .resultCode(info.resultCode())
                        .resultMsg(info.resultMsg())
                        .certName(info.certName())
                        .certBirthday(normalizeBirthday(info.certBirthday()))
                        .certGender(info.certGender())
                        .certPhone(info.certPhone())
                        .matchedYn("Y")
                        .matchFailReason(null)
                        .rawResJson(info.rawResJson())
                        .build();

        resultRepository.save(resultInfo);

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

        ContractSnapshot existing =
                snapshotRepository
                        .findByContractIdAndSnapshotType(contractId, snapshotType)
                        .orElse(null);

        ContractSnapshot snap =
                ContractSnapshot.builder()
                        .id(existing != null ? existing.id() : null)
                        .contractId(contractId)
                        .insurerId(insurerId)
                        .method(method)
                        .snapshotType(snapshotType)
                        .jsonSnapshot(jsonSnapshot)
                        .build();

        snapshotRepository.save(snap);
    }
}

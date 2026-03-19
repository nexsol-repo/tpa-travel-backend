package com.nexsol.tpa.core.domain.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexsol.tpa.core.domain.contract.ContractPeopleFinder;
import com.nexsol.tpa.core.domain.contract.ContractReader;
import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthCertService {

    private final ContractReader contractReader;
    private final ContractPeopleFinder peopleFinder;
    private final PortOneCertificationReader certificationReader;
    private final AuthCertWriter authCertWriter;
    private final AuthCertMatcher authCertMatcher;

    public AuthCertResult complete(
            AuthCertCommand cmd, String userAgent, String clientIp, String referer) {

        if (cmd.contractId() == null) {
            throw new CoreApiException(
                    CoreApiErrorType.INVALID_AUTH_REQUEST, "contractId is required");
        }
        if (cmd.impUid() == null || cmd.impUid().isBlank()) {
            throw new CoreApiException(CoreApiErrorType.INVALID_AUTH_REQUEST, "impUid is required");
        }

        Long contractId = cmd.contractId();
        String provider = defaultProvider(cmd.provider());

        TravelContractEntity contract = contractReader.getById(contractId);

        var logInfo =
                AuthCertLogInfo.builder()
                        .contractId(contractId)
                        .bizNum(cmd.bizNum())
                        .impUid(cmd.impUid())
                        .requestId(cmd.requestId())
                        .pathRoot(cmd.pathRoot())
                        .moid(cmd.moid())
                        .pg(cmd.pg())
                        .provider(provider)
                        .userAgent(userAgent)
                        .clientIp(clientIp)
                        .referer(referer)
                        .build();
        authCertWriter.createOrUpdateLog(logInfo);

        var cert = certificationReader.getCertification(cmd.impUid());

        var contractor = peopleFinder.findContractor(contractId);
        boolean matched = authCertMatcher.isMatched(contractor, cert);
        String matchFailReason = matched ? null : "계약자 정보 불일치";

        var resultInfo =
                AuthCertResultInfo.builder()
                        .provider(provider)
                        .moid(firstNonBlank(cmd.moid(), cert.getMerchantUid()))
                        .impUid(cert.getImpUid())
                        .requestId(cmd.requestId())
                        .uniqueKey(cert.getUniqueKey())
                        .resultStatus("SUCCESS")
                        .certName(cert.getName())
                        .certBirthday(cert.getBirthday())
                        .certGender(authCertMatcher.normalizeGender(cert.getGender()))
                        .certPhone(cert.getPhone())
                        .matched(matched)
                        .matchFailReason(matchFailReason)
                        .rawResJson(cert.getRawJson())
                        .build();

        return authCertWriter.saveResultAndUpdateContract(contractId, resultInfo);
    }

    @Transactional
    public AuthCertResult historyComplete(
            AuthCertHistoryCommand cmd, String userAgent, String clientIp, String referer) {

        if (cmd.impUid() == null || cmd.impUid().isBlank()) {
            throw new CoreApiException(CoreApiErrorType.INVALID_AUTH_REQUEST, "impUid is required");
        }

        String provider = defaultProvider(cmd.provider());
        String pathRoot = firstNonBlank(cmd.pathRoot(), "dsf6");

        var logInfo =
                AuthCertLogInfo.builder()
                        .contractId(0L)
                        .bizNum(cmd.bizNum())
                        .impUid(cmd.impUid())
                        .requestId(cmd.requestId())
                        .pathRoot(pathRoot)
                        .moid(cmd.moid())
                        .pg(cmd.pg())
                        .provider(provider)
                        .userAgent(userAgent)
                        .clientIp(clientIp)
                        .referer(referer)
                        .build();
        authCertWriter.createOrUpdateLog(logInfo);

        var cert = certificationReader.getCertification(cmd.impUid());

        String moid = firstNonBlank(cmd.moid(), cert.getMerchantUid());
        String uniqueKey = cert.getUniqueKey();

        if (uniqueKey == null || uniqueKey.isBlank()) {
            var failInfo =
                    AuthCertResultInfo.builder()
                            .provider(provider)
                            .moid(moid)
                            .impUid(cert.getImpUid())
                            .requestId(cmd.requestId())
                            .resultStatus("FAIL")
                            .resultCode("NO_UNIQUE_KEY")
                            .resultMsg("본인인증 uniqueKey가 없습니다.")
                            .certName(cert.getName())
                            .certBirthday(cert.getBirthday())
                            .certGender(authCertMatcher.normalizeGender(cert.getGender()))
                            .certPhone(cert.getPhone())
                            .rawResJson(cert.getRawJson())
                            .build();
            return authCertWriter.saveHistoryResult(failInfo);
        }

        var resultInfo =
                AuthCertResultInfo.builder()
                        .provider(provider)
                        .moid(moid)
                        .impUid(cert.getImpUid())
                        .requestId(cmd.requestId())
                        .uniqueKey(uniqueKey)
                        .resultStatus("SUCCESS")
                        .certName(cert.getName())
                        .certBirthday(cert.getBirthday())
                        .certGender(authCertMatcher.normalizeGender(cert.getGender()))
                        .certPhone(cert.getPhone())
                        .matched(true)
                        .rawResJson(cert.getRawJson())
                        .build();

        return authCertWriter.saveHistoryResult(resultInfo);
    }

    private String defaultProvider(String provider) {
        return (provider == null || provider.isBlank()) ? "DANAL_PASS" : provider;
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}

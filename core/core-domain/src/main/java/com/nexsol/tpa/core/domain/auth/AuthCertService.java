package com.nexsol.tpa.core.domain.auth;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.contract.ContractInfo;
import com.nexsol.tpa.core.domain.contract.ContractPeopleFinder;
import com.nexsol.tpa.core.domain.contract.ContractReader;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

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
            AuthCertification cmd, String userAgent, String clientIp, String referer) {

        if (cmd.contractId() == null) {
            throw new CoreException(CoreErrorType.INVALID_AUTH_REQUEST, "contractId is required");
        }
        if (cmd.impUid() == null || cmd.impUid().isBlank()) {
            throw new CoreException(CoreErrorType.INVALID_AUTH_REQUEST, "impUid is required");
        }

        Long contractId = cmd.contractId();
        String provider = defaultProvider(cmd.provider());

        ContractInfo contract = contractReader.getById(contractId);

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
                        .moid(firstNonBlank(cmd.moid(), cert.merchantUid()))
                        .impUid(cert.impUid())
                        .requestId(cmd.requestId())
                        .uniqueKey(cert.uniqueKey())
                        .resultStatus("SUCCESS")
                        .certName(cert.name())
                        .certBirthday(cert.birthday())
                        .certGender(authCertMatcher.normalizeGender(cert.gender()))
                        .certPhone(cert.phone())
                        .matched(matched)
                        .matchFailReason(matchFailReason)
                        .rawResJson(cert.rawJson())
                        .build();

        return authCertWriter.saveResultAndUpdateContract(contractId, resultInfo);
    }

    public AuthCertResult historyComplete(
            AuthCertHistory cmd, String userAgent, String clientIp, String referer) {

        if (cmd.impUid() == null || cmd.impUid().isBlank()) {
            throw new CoreException(CoreErrorType.INVALID_AUTH_REQUEST, "impUid is required");
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

        String moid = firstNonBlank(cmd.moid(), cert.merchantUid());
        String uniqueKey = cert.uniqueKey();

        if (uniqueKey == null || uniqueKey.isBlank()) {
            var failInfo =
                    AuthCertResultInfo.builder()
                            .provider(provider)
                            .moid(moid)
                            .impUid(cert.impUid())
                            .requestId(cmd.requestId())
                            .resultStatus("FAIL")
                            .resultCode("NO_UNIQUE_KEY")
                            .resultMsg("본인인증 uniqueKey가 없습니다.")
                            .certName(cert.name())
                            .certBirthday(cert.birthday())
                            .certGender(authCertMatcher.normalizeGender(cert.gender()))
                            .certPhone(cert.phone())
                            .rawResJson(cert.rawJson())
                            .build();
            return authCertWriter.saveHistoryResult(failInfo);
        }

        var resultInfo =
                AuthCertResultInfo.builder()
                        .provider(provider)
                        .moid(moid)
                        .impUid(cert.impUid())
                        .requestId(cmd.requestId())
                        .uniqueKey(uniqueKey)
                        .resultStatus("SUCCESS")
                        .certName(cert.name())
                        .certBirthday(cert.birthday())
                        .certGender(authCertMatcher.normalizeGender(cert.gender()))
                        .certPhone(cert.phone())
                        .matched(true)
                        .rawResJson(cert.rawJson())
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

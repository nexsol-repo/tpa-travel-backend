package com.nexsol.tpa.core.api.service.v1;

import com.nexsol.tpa.core.api.dto.v1.AuthCertCompleteRequest;
import com.nexsol.tpa.core.api.dto.v1.AuthCertHistoryCompleteRequest;
import com.nexsol.tpa.core.api.dto.v1.AuthCertResultResponse;
import com.nexsol.tpa.core.api.entity.TravelContractEntity;
import com.nexsol.tpa.core.api.external.portone.PortOneClient;
import com.nexsol.tpa.core.api.repository.v1.TravelContractRepository;
import com.nexsol.tpa.core.api.service.AuthCertPersistenceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthCertService {

    private final TravelContractRepository contractRepo;
    private final PortOneClient portOneClient;

    // 네 기존 것들(멱등 upsert 버전 권장)
    private final AuthCertPersistenceService persistence;
    // ↑ 아래 주석 참고: 기존 createOrUpdateLog/saveResultAndUpdateContract를 이 서비스에 두든,
    //    분리하든 상관없는데, 여기서는 “흐름 오케스트레이션”만 한다고 생각하면 됨.

    public AuthCertResultResponse complete(AuthCertCompleteRequest req,
                                           String userAgent,
                                           String clientIp,
                                           String referer) {

        Long contractId = req.getContractId();
        if (contractId == null) throw new IllegalArgumentException("contractId is required");
        if (req.getImpUid() == null || req.getImpUid().isBlank()) throw new IllegalArgumentException("impUid is required");

        // 0) 계약 존재 확인 (먼저 조회해서 insurerId null 등 정책 잡기)
        TravelContractEntity contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("계약 없음. contractId=" + contractId));

        // 1) 로그 upsert (impUid 기준)
        persistence.createOrUpdateLog(
                contractId,
                req.getBizNum(),
                req.getImpUid(),
                req.getRequestId(),
                req.getPathRoot(),
                req.getMoid(),
                req.getPg(),
                defaultProvider(req.getProvider()),
                userAgent,
                clientIp,
                referer,
                null // rawReqJson(원하면 req 전체 JSON으로 넣자)
        );

        // 2) PortOne 인증조회
        PortOneClient.CertificationResponse cert = portOneClient.getCertification(req.getImpUid());

        // 3) 서버에서 matched 계산(프론트 값 신뢰 X)
        boolean matched = isMatched(contract, cert);
        String matchFailReason = matched ? null : "계약자 정보 불일치";

        // 4) 결과/계약 업데이트/스냅샷 저장(멱등 upsert 권장)
        // resultStatus는 PortOne 조회가 성공했으니 SUCCESS로 두고,
        // 실패 케이스는 try/catch로 FAIL 저장하면 됨.
        return persistence.saveResultAndUpdateContract(
                contractId,
                defaultProvider(req.getProvider()),
                firstNonBlank(req.getMoid(), cert.getMerchantUid()),
                cert.getImpUid(),
                req.getRequestId(),
                cert.getUniqueKey(),
                "SUCCESS",
                null,
                null,
                cert.getName(),
                cert.getBirthday(),
                normalizeGender(cert.getGender()),
                cert.getPhone(),
                matched,
                matchFailReason,
                cert.getRawJson()
        );
    }

    @Transactional
    public AuthCertResultResponse historyComplete(
            AuthCertHistoryCompleteRequest req,
            String userAgent,
            String clientIp,
            String referer
    ) {
        if (req.getImpUid() == null || req.getImpUid().isBlank()) {
            throw new IllegalArgumentException("impUid is required");
        }

        String provider = defaultProvider(req.getProvider());
        String pathRoot = firstNonBlank(req.getPathRoot(), "dsf6");

        // 1) log upsert
        persistence.createOrUpdateLog(
                0L,                 // contractId 없음 → 0
                req.getBizNum(),
                req.getImpUid(),
                req.getRequestId(),
                pathRoot,
                req.getMoid(),
                req.getPg(),
                provider,
                userAgent,
                clientIp,
                referer,
                null
        );

        // 2) PortOne 인증조회
        PortOneClient.CertificationResponse cert = portOneClient.getCertification(req.getImpUid());

        String moid = firstNonBlank(req.getMoid(), cert.getMerchantUid());
        String uniqueKey = cert.getUniqueKey();

        // 3) uniqueKey 없으면 FAIL 저장 + 반환
        if (uniqueKey == null || uniqueKey.isBlank()) {
            return persistence.saveHistoryResult(
                    provider,
                    moid,
                    cert.getImpUid(),
                    req.getRequestId(),
                    null,
                    "FAIL",
                    "NO_UNIQUE_KEY",
                    "본인인증 uniqueKey가 없습니다.",
                    cert.getName(),
                    cert.getBirthday(),
                    normalizeGender(cert.getGender()),
                    cert.getPhone(),
                    cert.getRawJson()
            );
        }

        // 4) SUCCESS 저장 + 반환
        return persistence.saveHistoryResult(
                provider,
                moid,
                cert.getImpUid(),
                req.getRequestId(),
                uniqueKey,
                "SUCCESS",
                null,
                null,
                cert.getName(),
                cert.getBirthday(),
                normalizeGender(cert.getGender()),
                cert.getPhone(),
                cert.getRawJson()
        );
    }

    private String defaultProvider(String provider) {
        return (provider == null || provider.isBlank()) ? "DANAL_PASS" : provider;
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    private boolean isMatched(TravelContractEntity contract, PortOneClient.CertificationResponse cert) {
        // ⚠️ 실제 매칭 규칙은 너희 정책에 맞춰 조정
        // 최소: 이름 + 생년월일(또는 phone) 중 1개 이상 일치 추천
        String contractName = contract.getContractPeopleName();
        String contractRrn = contract.getContractPeopleResidentNumber(); // 암호화/마스킹이면 비교 불가 -> birth만 따로 저장하는게 좋음
        String birthFromContract = null;

        // TODO: contract에 생년월일 컬럼이 따로 없으면,
        // 주민번호(복호화 가능)에서 8자리 birth 추출하거나,
        // 아예 계약 생성 시 birth 컬럼 따로 저장하는 걸 추천.

        boolean nameOk = contractName != null && cert.getName() != null
                && normalizeName(contractName).equals(normalizeName(cert.getName()));

        boolean birthOk = birthFromContract != null && cert.getBirthday() != null
                && Objects.equals(birthFromContract, cert.getBirthday());

        // birth 추출이 지금 안 되면 이름+휴대폰이라도…
        boolean phoneOk = contract.getContractPeopleHp() != null && cert.getPhone() != null
                && normalizePhone(contract.getContractPeopleHp()).equals(normalizePhone(cert.getPhone()));

        // 우선순위: (이름 && (birth || phone))
        return nameOk && (birthOk || phoneOk);
    }

    private String normalizeName(String s) {
        return s.replaceAll("\\s+", "");
    }

    private String normalizePhone(String s) {
        return s.replaceAll("[^0-9]", "");
    }

    private String normalizeGender(String g) {
        if (g == null) return null;
        String x = g.trim().toUpperCase();
        if (x.equals("MALE") || x.equals("M") || x.equals("1")) return "M";
        if (x.equals("FEMALE") || x.equals("F") || x.equals("2")) return "F";
        return g;
    }
}

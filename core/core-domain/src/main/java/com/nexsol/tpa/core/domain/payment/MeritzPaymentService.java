package com.nexsol.tpa.core.domain.payment;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.core.domain.client.InsuranceContractClient.BridgeApiResult;
import com.nexsol.tpa.core.domain.client.InsurancePaymentClient;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeritzPaymentService {

    private static final String DEFAULT_COMPANY = "tpa";

    private final InsurancePaymentClient paymentClient;

    public BridgeApiResult approveCard(
            String company,
            String polNo,
            String quotGrpNo,
            String quotReqNo,
            String crdNo,
            String efctPrd,
            String dporNm,
            String dporCd,
            String rcptPrem) {

        String resolvedCompany = resolveCompany(company);
        BridgeApiResult res =
                paymentClient.approveCard(
                        resolvedCompany,
                        polNo,
                        quotGrpNo,
                        quotReqNo,
                        crdNo,
                        efctPrd,
                        dporNm,
                        dporCd,
                        rcptPrem);

        if (!res.success()) {
            throw new CoreException(
                    CoreErrorType.DEFAULT_ERROR,
                    "카드승인 실패. errCd=" + res.errCd() + ", errMsg=" + res.errMsg());
        }
        return res;
    }

    public BridgeApiResult cancelCard(
            String company, String polNo, String estNo, String orgApvNo, String cncAmt) {

        String resolvedCompany = resolveCompany(company);
        BridgeApiResult res =
                paymentClient.cancelCard(resolvedCompany, polNo, estNo, orgApvNo, cncAmt);

        if (!res.success()) {
            throw new CoreException(
                    CoreErrorType.DEFAULT_ERROR,
                    "카드취소 실패. errCd=" + res.errCd() + ", errMsg=" + res.errMsg());
        }
        return res;
    }

    private String resolveCompany(String company) {
        if (company == null || company.isBlank() || "TPA".equalsIgnoreCase(company)) {
            return DEFAULT_COMPANY;
        }
        if ("INSBOON".equalsIgnoreCase(company)) {
            return "insboon";
        }
        throw new CoreException(CoreErrorType.INVALID_REQUEST, "Unknown company: " + company);
    }
}

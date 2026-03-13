package com.nexsol.tpa.core.domain.payment;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeApiResponse;
import com.nexsol.tpa.client.meritz.payment.MeritzPaymentClient;
import com.nexsol.tpa.core.support.error.CoreApiErrorType;
import com.nexsol.tpa.core.support.error.CoreApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeritzPaymentService {

    private static final String DEFAULT_COMPANY = "tpa";

    private final MeritzPaymentClient paymentClient;

    public MeritzBridgeApiResponse approveCard(
            String company,
            String polNo,
            String estNo,
            String crdNo,
            String efctPrd,
            String dporNm,
            String dporCd,
            String apvAmt) {

        String resolvedCompany = resolveCompany(company);
        MeritzBridgeApiResponse res =
                paymentClient.approveCard(
                        resolvedCompany, polNo, estNo, crdNo, efctPrd, dporNm, dporCd, apvAmt);

        if (!res.isSuccess()) {
            throw new CoreApiException(
                    CoreApiErrorType.DEFAULT_ERROR,
                    "카드승인 실패. errCd=" + res.getErrCd() + ", errMsg=" + res.getErrMsg());
        }
        return res;
    }

    public MeritzBridgeApiResponse cancelCard(
            String company, String polNo, String estNo, String orgApvNo, String cncAmt) {

        String resolvedCompany = resolveCompany(company);
        MeritzBridgeApiResponse res =
                paymentClient.cancelCard(resolvedCompany, polNo, estNo, orgApvNo, cncAmt);

        if (!res.isSuccess()) {
            throw new CoreApiException(
                    CoreApiErrorType.DEFAULT_ERROR,
                    "카드취소 실패. errCd=" + res.getErrCd() + ", errMsg=" + res.getErrMsg());
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
        throw new CoreApiException(CoreApiErrorType.INVALID_REQUEST, "Unknown company: " + company);
    }
}

package com.nexsol.tpa.core.api.controller.v1;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;

import com.nexsol.tpa.client.meritz.bridge.dto.MeritzBridgeApiResponse;
import com.nexsol.tpa.core.api.controller.v1.request.ContractInquiryRequest;
import com.nexsol.tpa.core.api.controller.v1.request.ContractListRequest;
import com.nexsol.tpa.core.domain.apply.ApplyService;
import com.nexsol.tpa.core.domain.apply.ContractApply;
import com.nexsol.tpa.core.domain.cancel.CancelService;
import com.nexsol.tpa.core.domain.certificate.CertificateCommand;
import com.nexsol.tpa.core.domain.certificate.CertificateService;
import com.nexsol.tpa.core.domain.inquiry.InquiryService;
import com.nexsol.tpa.core.domain.refund.ContractRefund;
import com.nexsol.tpa.core.domain.subscription.SubscriptionCommand;
import com.nexsol.tpa.core.domain.subscription.SubscriptionResult;
import com.nexsol.tpa.core.domain.subscription.SubscriptionService;
import com.nexsol.tpa.test.api.RestDocsTest;

@Tag("restdocs")
class MeritzContractControllerDocsTest extends RestDocsTest {

    private final ApplyService applyService = mock(ApplyService.class);
    private final SubscriptionService subscriptionService = mock(SubscriptionService.class);
    private final CancelService cancelService = mock(CancelService.class);
    private final CertificateService certificateService = mock(CertificateService.class);
    private final InquiryService inquiryService = mock(InquiryService.class);

    @BeforeEach
    void setUpMockMvc(RestDocumentationContextProvider restDocumentation) {
        super.setUp(restDocumentation);
        this.mockMvc =
                mockController(
                        new MeritzContractController(
                                applyService,
                                subscriptionService,
                                cancelService,
                                certificateService,
                                inquiryService));
    }

    // ── Meritz Bridge 계약 조회 ──

    @Test
    void contractList() throws Exception {
        var response = new MeritzBridgeApiResponse();
        response.setSuccess(true);
        response.setData(
                List.of(
                        Map.of(
                                "polNo", "POL001",
                                "ctrStDt", "20260301",
                                "ctrEdDt", "20260310")));

        when(inquiryService.contractList(eq("TPA"), any(ContractListRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        get("/v1/meritz/contracts/list")
                                .param("company", "TPA")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                {
                  "header": {},
                  "body": {
                    "polNo": "POL001",
                    "ctrStDt": "20260301",
                    "ctrEdDt": "20260310"
                  }
                }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "meritz-contract-list",
                                queryParameters(
                                        parameterWithName("company")
                                                .description("회사코드 (기본값: TPA)")
                                                .optional()),
                                responseFields(bridgeResponseFields())));
    }

    @Test
    void contractInquiry() throws Exception {
        var response = new MeritzBridgeApiResponse();
        response.setSuccess(true);
        response.setData(Map.of("polNo", "POL001", "ctrNo", "CTR001"));

        when(inquiryService.contractDetail(eq("TPA"), any(ContractInquiryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/v1/meritz/contracts/inquiry")
                                .param("company", "TPA")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                {
                  "header": {},
                  "body": {
                    "polNo": "POL001",
                    "ctrNo": "CTR001"
                  }
                }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "meritz-contract-inquiry",
                                queryParameters(
                                        parameterWithName("company")
                                                .description("회사코드 (기본값: TPA)")
                                                .optional()),
                                responseFields(bridgeResponseFields())));
    }

    @Test
    void joinCertificate() throws Exception {
        var response = new MeritzBridgeApiResponse();
        response.setSuccess(true);
        response.setData(Map.of("rltLinkUrl", "https://example.com/cert"));

        when(certificateService.issue(eq("TPA"), any(CertificateCommand.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/v1/meritz/contracts/certificate")
                                .param("company", "TPA")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                {
                  "contractId": 100,
                  "otptDiv": "A",
                  "otptTpCd": "V"
                }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "meritz-contract-certificate",
                                queryParameters(
                                        parameterWithName("company")
                                                .description("회사코드 (기본값: TPA)")
                                                .optional()),
                                requestFields(
                                        fieldWithPath("contractId")
                                                .type(NUMBER)
                                                .description("계약 ID"),
                                        fieldWithPath("otptDiv")
                                                .type(STRING)
                                                .description("출력구분 (A: 가입증명서/국문, B: 피보험자별/영문+국문)"),
                                        fieldWithPath("otptTpCd")
                                                .type(STRING)
                                                .description("출력유형코드 (V: Viewer, D: Download)")),
                                responseFields(bridgeResponseFields())));
    }

    // ── 여행자보험 접수/결제/취소 ──

    @Test
    void apply() throws Exception {
        when(applyService.apply(any(ContractApply.class))).thenReturn(100L);

        mockMvc.perform(
                        post("/v1/meritz/travel/contract/apply")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                {
                  "insurerId": 1,
                  "partnerId": 1,
                  "partnerName": "TPA파트너",
                  "channelId": 1,
                  "channelName": "채널1",
                  "familyId": 1,
                  "meritzQuoteGroupNumber": "QG001",
                  "meritzQuoteRequestNumber": "QR001",
                  "countryCode": "JP",
                  "countryName": "일본",
                  "insureBeginDate": "2026-03-15",
                  "insureEndDate": "2026-03-20",
                  "totalPremium": 27000,
                  "people": [
                    {
                      "planId": 10,
                      "name": "홍길동",
                      "gender": "1",
                      "residentNumber": "9001011234567",
                      "englishName": "HONG GILDONG",
                      "passportNumber": "M12345678",
                      "phone": "01012345678",
                      "email": "hong@test.com",
                      "insurePremium": 15000
                    },
                    {
                      "planId": 11,
                      "name": "김영희",
                      "gender": "2",
                      "residentNumber": "9205152345678",
                      "englishName": "KIM YOUNGHEE",
                      "passportNumber": "M87654321",
                      "insurePremium": 12000
                    }
                  ],
                  "marketingConsentUsed": false
                }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "meritz-contract-apply",
                                requestFields(applyRequestFields()),
                                responseFields(
                                        apiResponseFields(
                                                fieldWithPath("data")
                                                        .type(NUMBER)
                                                        .description("계약 ID")))));
    }

    @Test
    void completed() throws Exception {
        var result =
                SubscriptionResult.success(
                        100L,
                        "해외여행자보험",
                        "가뿐한플랜",
                        LocalDate.of(2026, 3, 15),
                        LocalDate.of(2026, 3, 20),
                        "홍길동",
                        2);

        when(subscriptionService.subscribe(eq("TPA"), any(SubscriptionCommand.class)))
                .thenReturn(result);

        mockMvc.perform(
                        post("/v1/meritz/travel/contract/completed")
                                .param("company", "TPA")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                {
                  "contractId": 100,
                  "cardNo": "1234567890123456",
                  "efctPrd": "202803",
                  "dporNm": "홍길동",
                  "dporCd": "01"
                }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "meritz-contract-completed",
                                queryParameters(
                                        parameterWithName("company")
                                                .description("회사코드 (기본값: TPA)")
                                                .optional()),
                                requestFields(
                                        fieldWithPath("contractId")
                                                .type(NUMBER)
                                                .description("계약 ID"),
                                        fieldWithPath("cardNo").type(STRING).description("카드번호"),
                                        fieldWithPath("efctPrd")
                                                .type(STRING)
                                                .description("유효기간 (YYYYMM)"),
                                        fieldWithPath("dporNm").type(STRING).description("예금주명"),
                                        fieldWithPath("dporCd").type(STRING).description("예금주코드")),
                                responseFields(completedResponseFields())));
    }

    @Test
    void cancel() throws Exception {
        when(cancelService.cancel(eq("TPA"), any(ContractRefund.class))).thenReturn(100L);

        mockMvc.perform(
                        post("/v1/meritz/travel/contract/cancel")
                                .param("company", "TPA")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                {
                  "contractId": 100,
                  "refundMethod": "BANK",
                  "bankName": "국민은행",
                  "accountNumber": "123-456-789012",
                  "depositorName": "홍길동",
                  "refundReason": "단순 변심"
                }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "meritz-contract-cancel",
                                queryParameters(
                                        parameterWithName("company")
                                                .description("회사코드 (기본값: TPA)")
                                                .optional()),
                                requestFields(cancelRequestFields()),
                                responseFields(
                                        apiResponseFields(
                                                fieldWithPath("data")
                                                        .type(NUMBER)
                                                        .description("계약 ID")))));
    }

    // ── Request Field Descriptors ──

    private static org.springframework.restdocs.payload.FieldDescriptor[] applyRequestFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("insurerId").type(NUMBER).description("보험사 ID"),
            fieldWithPath("partnerId").type(NUMBER).description("파트너 ID"),
            fieldWithPath("partnerName").type(STRING).description("파트너명"),
            fieldWithPath("channelId").type(NUMBER).description("채널 ID"),
            fieldWithPath("channelName").type(STRING).description("채널명"),
            fieldWithPath("familyId").type(NUMBER).description("플랜 패밀리 ID"),
            fieldWithPath("meritzQuoteGroupNumber")
                    .type(STRING)
                    .description("메리츠 견적그룹번호")
                    .optional(),
            fieldWithPath("meritzQuoteRequestNumber")
                    .type(STRING)
                    .description("메리츠 견적요청번호")
                    .optional(),
            fieldWithPath("countryCode").type(STRING).description("국가코드"),
            fieldWithPath("countryName").type(STRING).description("국가명"),
            fieldWithPath("insureBeginDate").type(STRING).description("보험시작일 (YYYY-MM-DD)"),
            fieldWithPath("insureEndDate").type(STRING).description("보험종료일 (YYYY-MM-DD)"),
            fieldWithPath("totalPremium").type(NUMBER).description("총 보험료"),
            fieldWithPath("people[]").type(ARRAY).description("피보험자 목록"),
            fieldWithPath("people[].planId").type(NUMBER).description("피보험자 플랜 ID"),
            fieldWithPath("people[].name").type(STRING).description("피보험자 이름"),
            fieldWithPath("people[].gender").type(STRING).description("성별 (1: 남, 2: 여)"),
            fieldWithPath("people[].residentNumber").type(STRING).description("주민등록번호"),
            fieldWithPath("people[].englishName").type(STRING).description("영문이름"),
            fieldWithPath("people[].passportNumber").type(STRING).description("여권번호"),
            fieldWithPath("people[].phone").type(STRING).description("휴대폰번호").optional(),
            fieldWithPath("people[].email").type(STRING).description("이메일").optional(),
            fieldWithPath("people[].insurePremium").type(NUMBER).description("피보험자 보험료"),
            fieldWithPath("marketingConsentUsed").type(BOOLEAN).description("마케팅 동의 여부"),
        };
    }

    private static org.springframework.restdocs.payload.FieldDescriptor[] cancelRequestFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("contractId").type(NUMBER).description("계약 ID"),
            fieldWithPath("refundMethod").type(STRING).description("환불방법 (CARD, BANK, VBANK)"),
            fieldWithPath("bankName").type(STRING).description("은행명").optional(),
            fieldWithPath("accountNumber").type(STRING).description("계좌번호").optional(),
            fieldWithPath("depositorName").type(STRING).description("예금주명").optional(),
            fieldWithPath("refundReason").type(STRING).description("환불 사유").optional(),
        };
    }

    // ── Response Field Descriptors ──

    private static org.springframework.restdocs.payload.FieldDescriptor[] bridgeResponseFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
            fieldWithPath("errCd").type(STRING).description("에러 코드").optional(),
            fieldWithPath("errMsg").type(STRING).description("에러 메시지").optional(),
            subsectionWithPath("data").description("응답 데이터").optional(),
        };
    }

    private static org.springframework.restdocs.payload.FieldDescriptor[] apiResponseFields(
            org.springframework.restdocs.payload.FieldDescriptor... dataFields) {
        var base =
                new org.springframework.restdocs.payload.FieldDescriptor[] {
                    fieldWithPath("result").type(STRING).description("결과 (SUCCESS/ERROR)"),
                    fieldWithPath("error").type(OBJECT).description("에러 정보").optional(),
                };
        var result =
                new org.springframework.restdocs.payload.FieldDescriptor
                        [base.length + dataFields.length];
        System.arraycopy(base, 0, result, 0, base.length);
        System.arraycopy(dataFields, 0, result, base.length, dataFields.length);
        return result;
    }

    private static org.springframework.restdocs.payload.FieldDescriptor[]
            completedResponseFields() {
        return apiResponseFields(
                fieldWithPath("data.ok").type(BOOLEAN).description("성공 여부"),
                fieldWithPath("data.errCd").type(STRING).description("에러 코드").optional(),
                fieldWithPath("data.errMsg").type(STRING).description("에러 메시지").optional(),
                fieldWithPath("data.contractId").type(NUMBER).description("계약 ID"),
                fieldWithPath("data.insuranceProductName").type(STRING).description("보험상품명"),
                fieldWithPath("data.planName").type(STRING).description("플랜명"),
                fieldWithPath("data.insureStartDate").type(STRING).description("보험시작일"),
                fieldWithPath("data.insureEndDate").type(STRING).description("보험종료일"),
                fieldWithPath("data.contractPeopleName").type(STRING).description("대표계약자명"),
                fieldWithPath("data.insuredPeopleCount").type(NUMBER).description("가입인원수"));
    }
}

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;

import com.nexsol.tpa.core.domain.apply.ApplyService;
import com.nexsol.tpa.core.domain.apply.ContractApply;
import com.nexsol.tpa.core.domain.cancel.CancelService;
import com.nexsol.tpa.core.domain.certificate.CertificateCommand;
import com.nexsol.tpa.core.domain.certificate.CertificateLink;
import com.nexsol.tpa.core.domain.certificate.CertificateService;
import com.nexsol.tpa.core.domain.contract.ContractQueryService;
import com.nexsol.tpa.core.domain.inquiry.InquiryService;
import com.nexsol.tpa.core.domain.inquiry.InsuredContractDetail;
import com.nexsol.tpa.core.domain.inquiry.InsuredContractSummary;
import com.nexsol.tpa.core.domain.refund.ContractRefund;
import com.nexsol.tpa.core.domain.subscription.SubscriptionCommand;
import com.nexsol.tpa.core.domain.subscription.SubscriptionResult;
import com.nexsol.tpa.core.domain.subscription.SubscriptionService;
import com.nexsol.tpa.test.api.RestDocsTest;

@Tag("restdocs")
class ContractControllerDocsTest extends RestDocsTest {

    private final ApplyService applyService = mock(ApplyService.class);
    private final SubscriptionService subscriptionService = mock(SubscriptionService.class);
    private final CancelService cancelService = mock(CancelService.class);
    private final CertificateService certificateService = mock(CertificateService.class);
    private final InquiryService inquiryService = mock(InquiryService.class);
    private final ContractQueryService queryService = mock(ContractQueryService.class);

    @BeforeEach
    void setUpMockMvc(RestDocumentationContextProvider restDocumentation) {
        super.setUp(restDocumentation);
        this.mockMvc =
                mockController(
                        new ContractController(
                                applyService,
                                subscriptionService,
                                cancelService,
                                certificateService,
                                inquiryService,
                                queryService));
    }

    @Test
    void contractList() throws Exception {
        var response =
                List.of(
                        InsuredContractSummary.builder()
                                .polNo("POL001")
                                .insBgnDt("20260301")
                                .insEdDt("20260310")
                                .ttPrem("27000")
                                .stat("정상")
                                .build());

        when(inquiryService.contractList(eq("TPA"), any(java.util.Map.class))).thenReturn(response);

        mockMvc.perform(
                        get("/v1/travel/contracts/list")
                                .param("company", "TPA")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                { "polNo": "POL001", "ctrStDt": "20260301", "ctrEdDt": "20260310" }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "contract-list",
                                queryParameters(
                                        parameterWithName("company")
                                                .description("회사코드 (기본값: TPA)")
                                                .optional()),
                                responseFields(bridgeResponseFields())));
    }

    @Test
    void contractInquiry() throws Exception {
        var response =
                InsuredContractDetail.builder()
                        .polNo("POL001")
                        .quotGrpNo("QG001")
                        .quotReqNo("QR001")
                        .stat("정상")
                        .build();

        when(inquiryService.contractDetail(eq("TPA"), any(java.util.Map.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/v1/travel/contracts/inquiry")
                                .param("company", "TPA")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                { "polNo": "POL001", "ctrNo": "CTR001" }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "contract-inquiry",
                                queryParameters(
                                        parameterWithName("company")
                                                .description("회사코드 (기본값: TPA)")
                                                .optional()),
                                responseFields(bridgeResponseFields())));
    }

    @Test
    void joinCertificate() throws Exception {
        var response = new CertificateLink("https://example.com/cert");

        when(certificateService.issue(eq("TPA"), any(CertificateCommand.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/v1/travel/contracts/certificate")
                                .param("company", "TPA")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                { "contractId": 100, "otptDiv": "A", "otptTpCd": "V" }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "contract-certificate",
                                queryParameters(
                                        parameterWithName("company")
                                                .description("회사코드 (기본값: TPA)")
                                                .optional()),
                                requestFields(
                                        fieldWithPath("contractId")
                                                .type(NUMBER)
                                                .description("계약 ID"),
                                        fieldWithPath("otptDiv").type(STRING).description("출력구분"),
                                        fieldWithPath("otptTpCd")
                                                .type(STRING)
                                                .description("출력유형코드")),
                                responseFields(bridgeResponseFields())));
    }

    @Test
    void apply() throws Exception {
        when(applyService.apply(any(ContractApply.class))).thenReturn(100L);

        mockMvc.perform(
                        post("/v1/travel/contract/apply")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                {
                  "insurerId": 1, "partnerId": 1, "partnerName": "TPA파트너",
                  "channelId": 1, "channelName": "채널1", "familyId": 1,
                  "meritzQuoteGroupNumber": "QG001", "meritzQuoteRequestNumber": "QR001",
                  "countryCode": "JP", "countryName": "일본",
                  "insureBeginDate": "2026-03-15", "insureEndDate": "2026-03-20",
                  "totalPremium": 27000, "marketingConsentUsed": false,
                  "people": [
                    { "planId": 10, "name": "홍길동", "gender": "1",
                      "residentNumber": "9001011234567", "englishName": "HONG GILDONG",
                      "passportNumber": "M12345678", "phone": "01012345678",
                      "email": "hong@test.com", "insurePremium": 15000 }
                  ]
                }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "contract-apply",
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
                        post("/v1/travel/contract/completed")
                                .param("company", "TPA")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                { "contractId": 100, "cardNo": "1234567890123456",
                  "efctPrd": "202803", "dporNm": "홍길동", "dporCd": "01" }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "contract-completed",
                                queryParameters(
                                        parameterWithName("company")
                                                .description("회사코드 (기본값: TPA)")
                                                .optional()),
                                responseFields(completedResponseFields())));
    }

    @Test
    void cancel() throws Exception {
        when(cancelService.cancel(eq("TPA"), any(ContractRefund.class))).thenReturn(100L);

        mockMvc.perform(
                        post("/v1/travel/contract/cancel")
                                .param("company", "TPA")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                { "contractId": 100, "refundMethod": "BANK",
                  "bankName": "국민은행", "accountNumber": "123-456-789012",
                  "depositorName": "홍길동", "refundReason": "단순 변심" }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "contract-cancel",
                                queryParameters(
                                        parameterWithName("company")
                                                .description("회사코드 (기본값: TPA)")
                                                .optional()),
                                responseFields(
                                        apiResponseFields(
                                                fieldWithPath("data")
                                                        .type(NUMBER)
                                                        .description("계약 ID")))));
    }

    // ── Response Fields ──

    private static org.springframework.restdocs.payload.FieldDescriptor[] bridgeResponseFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("result").type(STRING).description("결과"),
            subsectionWithPath("data").description("응답 데이터").optional(),
            fieldWithPath("error").type(OBJECT).description("에러 정보").optional(),
        };
    }

    private static org.springframework.restdocs.payload.FieldDescriptor[] apiResponseFields(
            org.springframework.restdocs.payload.FieldDescriptor... dataFields) {
        var base =
                new org.springframework.restdocs.payload.FieldDescriptor[] {
                    fieldWithPath("result").type(STRING).description("결과"),
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

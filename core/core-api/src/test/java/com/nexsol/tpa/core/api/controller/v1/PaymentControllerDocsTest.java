package com.nexsol.tpa.core.api.controller.v1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;

import com.nexsol.tpa.core.domain.payment.CardApproval;
import com.nexsol.tpa.core.domain.payment.CardCancellation;
import com.nexsol.tpa.core.domain.payment.PaymentService;
import com.nexsol.tpa.test.api.RestDocsTest;

@Tag("restdocs")
class PaymentControllerDocsTest extends RestDocsTest {

    private final PaymentService service = mock(PaymentService.class);

    @BeforeEach
    void setUpMockMvc(RestDocumentationContextProvider restDocumentation) {
        super.setUp(restDocumentation);
        this.mockMvc = mockController(new PaymentController(service));
    }

    @Test
    void approveCard() throws Exception {
        var response =
                CardApproval.builder()
                        .approvalNumber("APV20260312001")
                        .approvalDate("20260312")
                        .build();

        when(service.approveCard(eq("TPA"), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/v1/travel/payments/cards/approve")
                                .param("company", "TPA")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                {
                  "polNo": "POL202603120001",
                  "quotGrpNo": "QG001",
                  "quotReqNo": "QR001",
                  "crdNo": "1234567890123456",
                  "efctPrd": "202803",
                  "dporNm": "홍길동",
                  "dporCd": "01",
                  "rcptPrem": "27000"
                }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "payment-card-approve",
                                queryParameters(
                                        parameterWithName("company")
                                                .description("회사코드 (기본값: TPA)")
                                                .optional()),
                                requestFields(
                                        fieldWithPath("polNo").type(STRING).description("증권번호"),
                                        fieldWithPath("quotGrpNo")
                                                .type(STRING)
                                                .description("견적그룹번호"),
                                        fieldWithPath("quotReqNo")
                                                .type(STRING)
                                                .description("견적요청번호"),
                                        fieldWithPath("crdNo").type(STRING).description("카드번호"),
                                        fieldWithPath("efctPrd")
                                                .type(STRING)
                                                .description("유효기간 (YYYYMM)"),
                                        fieldWithPath("dporNm").type(STRING).description("예금주명"),
                                        fieldWithPath("dporCd").type(STRING).description("예금주코드"),
                                        fieldWithPath("rcptPrem")
                                                .type(STRING)
                                                .description("영수보험료")),
                                responseFields(bridgeResponseFields())));
    }

    @Test
    void cancelCard() throws Exception {
        var response = CardCancellation.builder().cancellationNumber("CNC20260312001").build();

        when(service.cancelCard(eq("TPA"), any(), any(), any(), any())).thenReturn(response);

        mockMvc.perform(
                        post("/v1/travel/payments/cards/cancel")
                                .param("company", "TPA")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                {
                  "polNo": "POL202603120001",
                  "estNo": "EST001",
                  "orgApvNo": "APV20260312001",
                  "cncAmt": "27000"
                }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "payment-card-cancel",
                                queryParameters(
                                        parameterWithName("company")
                                                .description("회사코드 (기본값: TPA)")
                                                .optional()),
                                requestFields(
                                        fieldWithPath("polNo").type(STRING).description("증권번호"),
                                        fieldWithPath("estNo").type(STRING).description("견적번호"),
                                        fieldWithPath("orgApvNo").type(STRING).description("원승인번호"),
                                        fieldWithPath("cncAmt").type(STRING).description("취소금액")),
                                responseFields(bridgeResponseFields())));
    }

    private static org.springframework.restdocs.payload.FieldDescriptor[] bridgeResponseFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("result").type(STRING).description("결과"),
            subsectionWithPath("data").description("응답 데이터").optional(),
            fieldWithPath("error").type(OBJECT).description("에러 정보").optional(),
        };
    }
}

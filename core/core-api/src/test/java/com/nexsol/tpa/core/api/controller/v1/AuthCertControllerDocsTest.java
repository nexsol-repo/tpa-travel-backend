package com.nexsol.tpa.core.api.controller.v1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;

import com.nexsol.tpa.core.domain.auth.AuthCertCommand;
import com.nexsol.tpa.core.domain.auth.AuthCertHistoryCommand;
import com.nexsol.tpa.core.domain.auth.AuthCertResult;
import com.nexsol.tpa.core.domain.auth.AuthCertService;
import com.nexsol.tpa.test.api.RestDocsTest;

@Tag("restdocs")
class AuthCertControllerDocsTest extends RestDocsTest {

    private final AuthCertService authCertService = mock(AuthCertService.class);

    @BeforeEach
    void setUpMockMvc(RestDocumentationContextProvider restDocumentation) {
        super.setUp(restDocumentation);
        this.mockMvc = mockController(new AuthCertController(authCertService));
    }

    @Test
    void certComplete() throws Exception {
        when(authCertService.complete(any(AuthCertCommand.class), any(), any(), any()))
                .thenReturn(sampleResult());

        mockMvc.perform(
                        post("/v1/auth/cert/complete")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                {
                  "contractId": 100,
                  "impUid": "imp_123456789",
                  "requestId": "req_001",
                  "moid": "merchant_uid_001",
                  "bizNum": "1234567890",
                  "pathRoot": "dsf6",
                  "pg": "danal",
                  "provider": "DANAL_PASS"
                }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "auth-cert-complete",
                                requestFields(certCompleteRequestFields()),
                                responseFields(certResponseFields())));
    }

    @Test
    void certHistoryComplete() throws Exception {
        when(authCertService.historyComplete(
                        any(AuthCertHistoryCommand.class), any(), any(), any()))
                .thenReturn(sampleResult());

        mockMvc.perform(
                        post("/v1/auth/cert/history/complete")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                {
                  "impUid": "imp_123456789",
                  "requestId": "req_001",
                  "moid": "merchant_uid_001",
                  "pg": "danal",
                  "provider": "DANAL_PASS",
                  "pathRoot": "dsf6",
                  "bizNum": "1234567890"
                }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "auth-cert-history-complete",
                                requestFields(certHistoryRequestFields()),
                                responseFields(certResponseFields())));
    }

    private static org.springframework.restdocs.payload.FieldDescriptor[]
            certCompleteRequestFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("contractId").type(NUMBER).description("계약 ID"),
            fieldWithPath("impUid").type(STRING).description("PortOne imp_uid"),
            fieldWithPath("requestId").type(STRING).description("요청 ID"),
            fieldWithPath("moid").type(STRING).description("PortOne merchant_uid"),
            fieldWithPath("bizNum").type(STRING).description("사업자번호"),
            fieldWithPath("pathRoot").type(STRING).description("경로 루트"),
            fieldWithPath("pg").type(STRING).description("PG사"),
            fieldWithPath("provider").type(STRING).description("본인인증 제공사"),
        };
    }

    private static org.springframework.restdocs.payload.FieldDescriptor[]
            certHistoryRequestFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("impUid").type(STRING).description("PortOne imp_uid"),
            fieldWithPath("requestId").type(STRING).description("요청 ID").optional(),
            fieldWithPath("moid").type(STRING).description("PortOne merchant_uid").optional(),
            fieldWithPath("pg").type(STRING).description("PG사").optional(),
            fieldWithPath("provider").type(STRING).description("본인인증 제공사").optional(),
            fieldWithPath("pathRoot").type(STRING).description("경로 루트").optional(),
            fieldWithPath("bizNum").type(STRING).description("사업자번호"),
        };
    }

    private static org.springframework.restdocs.payload.FieldDescriptor[] certResponseFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("result").type(STRING).description("결과"),
            fieldWithPath("data").type(OBJECT).description("인증 결과"),
            fieldWithPath("error").type(OBJECT).description("에러 정보").optional(),
            fieldWithPath("data.moid").type(STRING).description("merchant_uid"),
            fieldWithPath("data.impUid").type(STRING).description("imp_uid"),
            fieldWithPath("data.requestId").type(STRING).description("요청 ID"),
            fieldWithPath("data.insuredSeqNo").type(NUMBER).description("피보험자 순번").optional(),
            fieldWithPath("data.uniqueKey").type(STRING).description("고유키"),
            fieldWithPath("data.resultStatus").type(STRING).description("결과 상태"),
            fieldWithPath("data.resultCode").type(STRING).description("결과 코드"),
            fieldWithPath("data.resultMsg").type(STRING).description("결과 메시지"),
            fieldWithPath("data.certName").type(STRING).description("인증 이름"),
            fieldWithPath("data.certBirthday").type(STRING).description("인증 생년월일"),
            fieldWithPath("data.certGender").type(STRING).description("인증 성별"),
            fieldWithPath("data.certPhone").type(STRING).description("인증 휴대폰"),
            fieldWithPath("data.matchedYn").type(STRING).description("매칭 여부"),
            fieldWithPath("data.matchFailReason").type(STRING).description("매칭 실패 사유").optional(),
        };
    }

    private AuthCertResult sampleResult() {
        return new AuthCertResult(
                "merchant_uid_001",
                "imp_123456789",
                "req_001",
                null,
                "unique_key_abc123",
                "success",
                "0000",
                "인증 성공",
                "홍길동",
                "19900101",
                "1",
                "01012345678",
                "Y",
                null);
    }
}

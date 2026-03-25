package com.nexsol.tpa.core.api.controller.v1;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.RestDocumentationContextProvider;

import com.nexsol.tpa.core.domain.coverage.CoverageResult;
import com.nexsol.tpa.core.domain.coverage.CoverageService;
import com.nexsol.tpa.test.api.RestDocsTest;

@Tag("restdocs")
class CoverageControllerDocsTest extends RestDocsTest {

    private final CoverageService coverageService = mock(CoverageService.class);

    @BeforeEach
    void setUpMockMvc(RestDocumentationContextProvider restDocumentation) {
        super.setUp(restDocumentation);
        this.mockMvc = mockController(new CoverageController(coverageService));
    }

    @Test
    void getCoverage() throws Exception {
        var result =
                new CoverageResult(
                        1L,
                        "151910",
                        "해외상해사망",
                        "DEATH_DISABILITY",
                        "사망 또는 장해가 생겼을 때",
                        "상해로 인한 사망 시",
                        "보험가입금액 전액 지급",
                        "해외여행 중 상해사망",
                        "해외여행 중 상해로 사망한 경우 보험가입금액을 지급합니다.");

        when(coverageService.getCoverage(eq(1L), eq("151910"))).thenReturn(result);

        mockMvc.perform(
                        get("/v1/travel/coverages")
                                .param("insurerId", "1")
                                .param("coverageCode", "151910"))
                .andDo(print())
                .andDo(
                        document(
                                "coverage",
                                queryParameters(
                                        parameterWithName("insurerId").description("보험사 ID"),
                                        parameterWithName("coverageCode").description("담보코드")),
                                responseFields(
                                        fieldWithPath("result").type(STRING).description("결과"),
                                        fieldWithPath("data.id").type(NUMBER).description("담보 ID"),
                                        fieldWithPath("data.coverageCode")
                                                .type(STRING)
                                                .description("담보코드"),
                                        fieldWithPath("data.coverageName")
                                                .type(STRING)
                                                .description("담보명"),
                                        fieldWithPath("data.sectionCode")
                                                .type(STRING)
                                                .description("보장 섹션 코드"),
                                        fieldWithPath("data.sectionName")
                                                .type(STRING)
                                                .description("보장 섹션명"),
                                        fieldWithPath("data.claimReason")
                                                .type(STRING)
                                                .description("청구사유"),
                                        fieldWithPath("data.claimContent")
                                                .type(STRING)
                                                .description("청구내용"),
                                        fieldWithPath("data.subTitle")
                                                .type(STRING)
                                                .description("부제목"),
                                        fieldWithPath("data.subContent")
                                                .type(STRING)
                                                .description("부내용"),
                                        fieldWithPath("error")
                                                .type(OBJECT)
                                                .description("에러 정보")
                                                .optional())));
    }
}

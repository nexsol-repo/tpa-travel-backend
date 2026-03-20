package com.nexsol.tpa.core.api.controller.v1;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.RestDocumentationContextProvider;

import com.nexsol.tpa.core.api.controller.v1.response.ContractQueryResponse;
import com.nexsol.tpa.core.domain.contract.*;
import com.nexsol.tpa.core.domain.contract.TravelContractQueryService;
import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.plan.Insurer;
import com.nexsol.tpa.test.api.RestDocsTest;

@Tag("restdocs")
class TravelContractControllerDocsTest extends RestDocsTest {

    private final TravelContractQueryService queryService = mock(TravelContractQueryService.class);

    @BeforeEach
    void setUpMockMvc(RestDocumentationContextProvider restDocumentation) {
        super.setUp(restDocumentation);
        this.mockMvc = mockController(new TravelContractController(queryService));
    }

    @Test
    void contractListByAuthKey() throws Exception {
        var item =
                ContractQueryResponse.ContractListItem.builder()
                        .id(100L)
                        .policyNumber("POL001")
                        .totalPremium(new BigDecimal("27000"))
                        .status("COMPLETED")
                        .insuredPeopleNumber(2)
                        .termsUrl("https://example.com/terms.pdf")
                        .insurer(Insurer.builder().id(1L).name("메리츠화재").build())
                        .partner(Partner.builder().id(1L).name("TPA파트너").build())
                        .channel(Channel.builder().id(1L).name("채널1").build())
                        .plan(InsurancePlan.builder().id(10L).planName("가뿐한플랜").build())
                        .insurePeriod(
                                InsurePeriod.builder()
                                        .startDate(LocalDate.of(2026, 3, 15))
                                        .endDate(LocalDate.of(2026, 3, 20))
                                        .countryCode("JP")
                                        .countryName("일본")
                                        .build())
                        .auth(AuthInfo.builder().build())
                        .contractor(
                                Contractor.builder()
                                        .name("홍길동")
                                        .phone("01012345678")
                                        .email("hong@test.com")
                                        .build())
                        .payment(
                                Payment.builder()
                                        .id(1L)
                                        .paymentMethod("CARD")
                                        .status("PAID")
                                        .paidAmount(new BigDecimal("27000"))
                                        .build())
                        .people(List.of(new PersonSummary(1L, "홍길동")))
                        .build();

        var page = new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1);
        when(queryService.list(eq("unique_key_abc"), eq(0), eq(20))).thenReturn(page);

        mockMvc.perform(
                        get("/v1/meritz/travel/contracts")
                                .param("authUniqueKey", "unique_key_abc")
                                .param("page", "0")
                                .param("size", "20"))
                .andDo(print())
                .andDo(
                        document(
                                "travel-contracts",
                                queryParameters(
                                        parameterWithName("authUniqueKey")
                                                .description("인증 고유키")
                                                .optional(),
                                        parameterWithName("page")
                                                .description("페이지 번호 (기본값: 0)")
                                                .optional(),
                                        parameterWithName("size")
                                                .description("페이지 크기 (기본값: 20)")
                                                .optional()),
                                responseFields(contractListResponseFields())));
    }

    @Test
    void contractDetail() throws Exception {
        var detail = sampleContractDetail();
        when(queryService.get(eq(100L))).thenReturn(detail);

        mockMvc.perform(get("/v1/meritz/travel/contracts/{id}", 100L))
                .andDo(print())
                .andDo(
                        document(
                                "travel-contract-detail",
                                pathParameters(parameterWithName("id").description("계약 ID")),
                                responseFields(contractDetailResponseFields())));
    }

    // ── Response Field Descriptors ──

    private static org.springframework.restdocs.payload.FieldDescriptor[]
            contractListResponseFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            // ApiResponse wrapper
            fieldWithPath("result").type(STRING).description("결과 (SUCCESS/ERROR)"),
            fieldWithPath("error").type(OBJECT).description("에러 정보").optional(),
            // ContractListItem
            fieldWithPath("data.content[].id").type(NUMBER).description("계약 ID"),
            fieldWithPath("data.content[].policyNumber")
                    .type(STRING)
                    .description("증권번호")
                    .optional(),
            fieldWithPath("data.content[].totalPremium")
                    .type(NUMBER)
                    .description("총 보험료")
                    .optional(),
            fieldWithPath("data.content[].status").type(STRING).description("계약 상태").optional(),
            fieldWithPath("data.content[].insuredPeopleNumber").type(NUMBER).description("피보험자 수"),
            fieldWithPath("data.content[].applyDate").type(STRING).description("접수일시").optional(),
            fieldWithPath("data.content[].termsUrl").type(STRING).description("약관 URL").optional(),
            fieldWithPath("data.content[].policyLink").type(STRING).description("증권 링크").optional(),
            // Insurer
            subsectionWithPath("data.content[].insurer")
                    .type(OBJECT)
                    .description("보험사 정보")
                    .optional(),
            // Partner
            subsectionWithPath("data.content[].partner")
                    .type(OBJECT)
                    .description("파트너 정보")
                    .optional(),
            // Channel
            subsectionWithPath("data.content[].channel")
                    .type(OBJECT)
                    .description("채널 정보")
                    .optional(),
            // InsurancePlan
            subsectionWithPath("data.content[].plan").type(OBJECT).description("플랜 정보").optional(),
            // InsurePeriod
            subsectionWithPath("data.content[].insurePeriod")
                    .type(OBJECT)
                    .description("보험기간 정보")
                    .optional(),
            // AuthInfo
            subsectionWithPath("data.content[].auth").type(OBJECT).description("인증 정보").optional(),
            // Contractor
            subsectionWithPath("data.content[].contractor")
                    .type(OBJECT)
                    .description("대표계약자 정보")
                    .optional(),
            // Payment
            subsectionWithPath("data.content[].payment")
                    .type(OBJECT)
                    .description("결제 정보")
                    .optional(),
            // People
            fieldWithPath("data.content[].people[].id").type(NUMBER).description("피보험자 ID"),
            fieldWithPath("data.content[].people[].name").type(STRING).description("피보험자 이름"),
            // Page metadata
            subsectionWithPath("data.pageable").description("페이지 정보"),
            fieldWithPath("data.totalElements").type(NUMBER).description("전체 건수"),
            fieldWithPath("data.totalPages").type(NUMBER).description("전체 페이지 수"),
            fieldWithPath("data.size").type(NUMBER).description("페이지 크기"),
            fieldWithPath("data.number").type(NUMBER).description("현재 페이지"),
            fieldWithPath("data.sort").type(OBJECT).description("정렬 정보"),
            fieldWithPath("data.sort.empty").type(BOOLEAN).description("정렬 비어있음"),
            fieldWithPath("data.sort.sorted").type(BOOLEAN).description("정렬됨"),
            fieldWithPath("data.sort.unsorted").type(BOOLEAN).description("미정렬"),
            fieldWithPath("data.first").type(BOOLEAN).description("첫 페이지 여부"),
            fieldWithPath("data.last").type(BOOLEAN).description("마지막 페이지 여부"),
            fieldWithPath("data.numberOfElements").type(NUMBER).description("현재 페이지 건수"),
            fieldWithPath("data.empty").type(BOOLEAN).description("비어있음"),
        };
    }

    private static org.springframework.restdocs.payload.FieldDescriptor[]
            contractDetailResponseFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            // ApiResponse wrapper
            fieldWithPath("result").type(STRING).description("결과 (SUCCESS/ERROR)"),
            fieldWithPath("error").type(OBJECT).description("에러 정보").optional(),
            // ContractDetail
            subsectionWithPath("data.contract").type(OBJECT).description("계약 정보"),
            subsectionWithPath("data.insurer").type(OBJECT).description("보험사 정보").optional(),
            subsectionWithPath("data.partner").type(OBJECT).description("파트너 정보").optional(),
            subsectionWithPath("data.channel").type(OBJECT).description("채널 정보").optional(),
            subsectionWithPath("data.plan").type(OBJECT).description("플랜 정보").optional(),
            subsectionWithPath("data.payment").type(OBJECT).description("결제 정보").optional(),
            fieldWithPath("data.termsUrl").type(STRING).description("약관 URL").optional(),
            fieldWithPath("data.policyLink").type(STRING).description("증권 링크").optional(),
            fieldWithPath("data.people[].id").type(NUMBER).description("피보험자 ID"),
            fieldWithPath("data.people[].planId").type(NUMBER).description("플랜 ID").optional(),
            fieldWithPath("data.people[].isContractor").type(BOOLEAN).description("대표계약자 여부"),
            fieldWithPath("data.people[].name").type(STRING).description("이름"),
            fieldWithPath("data.people[].englishName").type(STRING).description("영문이름").optional(),
            fieldWithPath("data.people[].gender").type(STRING).description("성별"),
            fieldWithPath("data.people[].residentNumberMasked")
                    .type(STRING)
                    .description("주민번호 (마스킹)")
                    .optional(),
            fieldWithPath("data.people[].passportNumberMasked")
                    .type(STRING)
                    .description("여권번호 (마스킹)")
                    .optional(),
            fieldWithPath("data.people[].insurePremium")
                    .type(NUMBER)
                    .description("피보험자 보험료")
                    .optional(),
        };
    }

    // ── Sample Data ──

    private ContractQueryResponse.ContractDetail sampleContractDetail() {
        return ContractQueryResponse.ContractDetail.builder()
                .contract(
                        ContractInfo.builder()
                                .id(100L)
                                .familyId(1L)
                                .policyNumber("POL001")
                                .status("COMPLETED")
                                .totalPremium(new BigDecimal("27000"))
                                .insurePeriod(
                                        InsurePeriod.builder()
                                                .startDate(LocalDate.of(2026, 3, 15))
                                                .endDate(LocalDate.of(2026, 3, 20))
                                                .countryCode("JP")
                                                .countryName("일본")
                                                .build())
                                .contractor(
                                        Contractor.builder()
                                                .name("홍길동")
                                                .phone("01012345678")
                                                .email("hong@test.com")
                                                .build())
                                .auth(AuthInfo.builder().build())
                                .build())
                .insurer(Insurer.builder().id(1L).code("MERITZ").name("메리츠화재").build())
                .partner(Partner.builder().id(1L).code("TPA").name("TPA파트너").build())
                .channel(Channel.builder().id(1L).code("CH01").name("채널1").build())
                .plan(
                        InsurancePlan.builder()
                                .id(10L)
                                .planName("가뿐한플랜")
                                .planGroupCode("GRP01")
                                .planCode("TA2")
                                .build())
                .payment(
                        Payment.builder()
                                .id(1L)
                                .paymentMethod("CARD")
                                .status("PAID")
                                .paidAmount(new BigDecimal("27000"))
                                .build())
                .people(
                        List.of(
                                InsuredPerson.builder()
                                        .id(1L)
                                        .name("홍길동")
                                        .gender("1")
                                        .isContractor(true)
                                        .residentNumberMasked("900101-*******")
                                        .build(),
                                InsuredPerson.builder()
                                        .id(2L)
                                        .name("김영희")
                                        .gender("2")
                                        .isContractor(false)
                                        .residentNumberMasked("920515-*******")
                                        .build()))
                .build();
    }
}

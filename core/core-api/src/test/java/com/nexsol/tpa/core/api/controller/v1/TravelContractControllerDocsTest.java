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

import com.nexsol.tpa.core.api.dto.v1.contract.TravelContractQueryDto;
import com.nexsol.tpa.core.domain.contract.TravelContractQueryService;
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
                TravelContractQueryDto.ContractListItem.builder()
                        .id(100L)
                        .insurerId(1L)
                        .insurerName("메리츠화재")
                        .partnerId(1L)
                        .partnerName("TPA파트너")
                        .channelId(1L)
                        .channelName("채널1")
                        .planId(10L)
                        .planName("가뿐한플랜B")
                        .policyNumber("POL001")
                        .countryName("일본")
                        .countryCode("JP")
                        .insuredPeopleNumber(2)
                        .totalPremium(new BigDecimal("27000"))
                        .status("COMPLETED")
                        .insureStartDate(LocalDate.of(2026, 3, 15))
                        .insureEndDate(LocalDate.of(2026, 3, 20))
                        .contractPeopleName("홍길동")
                        .contractPeopleHp("01012345678")
                        .contractPeopleMail("hong@test.com")
                        .termsUrl("https://example.com/terms.pdf")
                        .payment(
                                TravelContractQueryDto.Payment.builder()
                                        .id(1L)
                                        .paymentMethod("CARD")
                                        .status("PAID")
                                        .paidAmount(new BigDecimal("27000"))
                                        .build())
                        .people(
                                List.of(
                                        TravelContractQueryDto.PersonSummary.builder()
                                                .id(1L)
                                                .name("홍길동")
                                                .build()))
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
            fieldWithPath("content[].id").type(NUMBER).description("계약 ID"),
            fieldWithPath("content[].insurerId").type(NUMBER).description("보험사 ID").optional(),
            fieldWithPath("content[].insurerName").type(STRING).description("보험사명").optional(),
            fieldWithPath("content[].partnerId").type(NUMBER).description("파트너 ID").optional(),
            fieldWithPath("content[].partnerName").type(STRING).description("파트너명").optional(),
            fieldWithPath("content[].channelId").type(NUMBER).description("채널 ID").optional(),
            fieldWithPath("content[].channelName").type(STRING).description("채널명").optional(),
            fieldWithPath("content[].planId").type(NUMBER).description("플랜 ID").optional(),
            fieldWithPath("content[].planName").type(STRING).description("플랜명").optional(),
            fieldWithPath("content[].policyNumber").type(STRING).description("증권번호").optional(),
            fieldWithPath("content[].countryName").type(STRING).description("국가명").optional(),
            fieldWithPath("content[].countryCode").type(STRING).description("국가코드").optional(),
            fieldWithPath("content[].insuredPeopleNumber")
                    .type(NUMBER)
                    .description("피보험자 수")
                    .optional(),
            fieldWithPath("content[].totalPremium").type(NUMBER).description("총 보험료").optional(),
            fieldWithPath("content[].status").type(STRING).description("계약 상태").optional(),
            fieldWithPath("content[].authUniqueKey").type(STRING).description("인증 고유키").optional(),
            fieldWithPath("content[].authStatus").type(STRING).description("인증 상태").optional(),
            fieldWithPath("content[].authDate").type(STRING).description("인증일시").optional(),
            fieldWithPath("content[].applyDate").type(STRING).description("접수일시").optional(),
            fieldWithPath("content[].insureStartDate").type(STRING).description("보험시작일").optional(),
            fieldWithPath("content[].insureEndDate").type(STRING).description("보험종료일").optional(),
            fieldWithPath("content[].contractPeopleName")
                    .type(STRING)
                    .description("계약자명")
                    .optional(),
            fieldWithPath("content[].contractPeopleResidentNumberMasked")
                    .type(STRING)
                    .description("계약자 주민번호 (마스킹)")
                    .optional(),
            fieldWithPath("content[].contractPeopleHp")
                    .type(STRING)
                    .description("계약자 휴대폰")
                    .optional(),
            fieldWithPath("content[].contractPeopleMail")
                    .type(STRING)
                    .description("계약자 이메일")
                    .optional(),
            fieldWithPath("content[].termsUrl").type(STRING).description("약관 URL").optional(),
            fieldWithPath("content[].policyLink").type(STRING).description("증권 링크").optional(),
            subsectionWithPath("content[].payment").type(OBJECT).description("결제 정보").optional(),
            fieldWithPath("content[].people[].id").type(NUMBER).description("피보험자 ID"),
            fieldWithPath("content[].people[].name").type(STRING).description("피보험자 이름"),
            // Page metadata
            subsectionWithPath("pageable").description("페이지 정보"),
            fieldWithPath("totalElements").type(NUMBER).description("전체 건수"),
            fieldWithPath("totalPages").type(NUMBER).description("전체 페이지 수"),
            fieldWithPath("size").type(NUMBER).description("페이지 크기"),
            fieldWithPath("number").type(NUMBER).description("현재 페이지"),
            fieldWithPath("sort").type(OBJECT).description("정렬 정보"),
            fieldWithPath("sort.empty").type(BOOLEAN).description("정렬 비어있음"),
            fieldWithPath("sort.sorted").type(BOOLEAN).description("정렬됨"),
            fieldWithPath("sort.unsorted").type(BOOLEAN).description("미정렬"),
            fieldWithPath("first").type(BOOLEAN).description("첫 페이지 여부"),
            fieldWithPath("last").type(BOOLEAN).description("마지막 페이지 여부"),
            fieldWithPath("numberOfElements").type(NUMBER).description("현재 페이지 건수"),
            fieldWithPath("empty").type(BOOLEAN).description("비어있음"),
        };
    }

    private static org.springframework.restdocs.payload.FieldDescriptor[]
            contractDetailResponseFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            subsectionWithPath("contract").type(OBJECT).description("계약 정보"),
            subsectionWithPath("insurer").type(OBJECT).description("보험사 정보").optional(),
            subsectionWithPath("partner").type(OBJECT).description("파트너 정보").optional(),
            subsectionWithPath("channel").type(OBJECT).description("채널 정보").optional(),
            subsectionWithPath("plan").type(OBJECT).description("플랜 정보").optional(),
            subsectionWithPath("payment").type(OBJECT).description("결제 정보").optional(),
            fieldWithPath("termsUrl").type(STRING).description("약관 URL").optional(),
            fieldWithPath("policyLink").type(STRING).description("증권 링크").optional(),
            fieldWithPath("people[].id").type(NUMBER).description("피보험자 ID"),
            fieldWithPath("people[].name").type(STRING).description("이름"),
            fieldWithPath("people[].nameEng").type(STRING).description("영문이름").optional(),
            fieldWithPath("people[].gender").type(STRING).description("성별"),
            fieldWithPath("people[].residentNumberMasked")
                    .type(STRING)
                    .description("주민번호 (마스킹)")
                    .optional(),
            fieldWithPath("people[].passportNumberMasked")
                    .type(STRING)
                    .description("여권번호 (마스킹)")
                    .optional(),
            fieldWithPath("people[].policyNumber").type(STRING).description("증권번호").optional(),
            fieldWithPath("people[].insurePremium").type(NUMBER).description("피보험자 보험료").optional(),
        };
    }

    // ── Sample Data ──

    private TravelContractQueryDto.ContractDetail sampleContractDetail() {
        return TravelContractQueryDto.ContractDetail.builder()
                .contract(
                        TravelContractQueryDto.Contract.builder()
                                .id(100L)
                                .insurerId(1L)
                                .partnerId(1L)
                                .channelId(1L)
                                .planId(10L)
                                .policyNumber("POL001")
                                .countryName("일본")
                                .countryCode("JP")
                                .status("COMPLETED")
                                .insureStartDate(LocalDate.of(2026, 3, 15))
                                .insureEndDate(LocalDate.of(2026, 3, 20))
                                .totalPremium(new BigDecimal("27000"))
                                .contractPeopleName("홍길동")
                                .contractPeopleHp("01012345678")
                                .contractPeopleMail("hong@test.com")
                                .build())
                .insurer(
                        TravelContractQueryDto.Insurer.builder()
                                .id(1L)
                                .insurerCode("MERITZ")
                                .insurerName("메리츠화재")
                                .build())
                .partner(
                        TravelContractQueryDto.Partner.builder()
                                .id(1L)
                                .partnerCode("TPA")
                                .partnerName("TPA파트너")
                                .build())
                .channel(
                        TravelContractQueryDto.Channel.builder()
                                .id(1L)
                                .channelCode("CH01")
                                .channelName("채널1")
                                .build())
                .plan(
                        TravelContractQueryDto.Plan.builder()
                                .id(10L)
                                .planName("가뿐한플랜B")
                                .planGroupCode("GRP01")
                                .planCode("TA2")
                                .build())
                .payment(
                        TravelContractQueryDto.Payment.builder()
                                .id(1L)
                                .paymentMethod("CARD")
                                .status("PAID")
                                .paidAmount(new BigDecimal("27000"))
                                .build())
                .people(
                        List.of(
                                TravelContractQueryDto.Person.builder()
                                        .id(1L)
                                        .name("홍길동")
                                        .gender("1")
                                        .residentNumberMasked("900101-*******")
                                        .build(),
                                TravelContractQueryDto.Person.builder()
                                        .id(2L)
                                        .name("김영희")
                                        .gender("2")
                                        .residentNumberMasked("920515-*******")
                                        .build()))
                .build();
    }
}

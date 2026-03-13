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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;

import com.nexsol.tpa.core.domain.coverage.TravelCoverageService;
import com.nexsol.tpa.core.domain.plan.TravelPlanReader.PlanFamily;
import com.nexsol.tpa.core.domain.plan.TravelPlanService;
import com.nexsol.tpa.core.domain.premium.PlanCondition;
import com.nexsol.tpa.core.domain.premium.PremiumResult;
import com.nexsol.tpa.core.domain.premium.PremiumService;
import com.nexsol.tpa.core.domain.premium.QuoteResult;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.test.api.RestDocsTest;

@Tag("restdocs")
class TravelPlanControllerDocsTest extends RestDocsTest {

    private final TravelPlanService planService = mock(TravelPlanService.class);
    private final TravelCoverageService coverageService = mock(TravelCoverageService.class);
    private final PremiumService premiumService = mock(PremiumService.class);

    @BeforeEach
    void setUpMockMvc(RestDocumentationContextProvider restDocumentation) {
        super.setUp(restDocumentation);
        this.mockMvc =
                mockController(
                        new TravelPlanController(planService, coverageService, premiumService));
    }

    @Test
    void plans() throws Exception {
        when(planService.findQuoteFamilies(any(PlanCondition.class))).thenReturn(sampleFamilies());
        when(premiumService.calculateAll(any(), any())).thenReturn(samplePremiumMap());
        when(coverageService.findCoveragesForFamilies(any())).thenReturn(sampleCoverageMap());

        mockMvc.perform(
                        post("/v1/meritz/travel/plans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                {
                  "insurerId": 1,
                  "insBgnDt": "20260315",
                  "insEdDt": "20260320",
                  "trvArCd": "JP",
                  "representativeIndex": 0,
                  "insuredList": [
                    { "birth": "19900101", "gender": "1" },
                    { "birth": "19920515", "gender": "2" }
                  ]
                }
                """))
                .andDo(print())
                .andDo(
                        document(
                                "meritz-travel-plans",
                                requestFields(quoteRequestFields()),
                                responseFields(planListResponseFields())));
    }

    @Test
    void planCoverages() throws Exception {
        var family = sampleFamilies().get(0);
        when(planService.findFamilyByPlanId(any(PlanCondition.class), eq(10L))).thenReturn(family);
        when(premiumService.calculateSingle(any(), any(), eq(0)))
                .thenReturn(samplePremiumMap().get(10L));
        when(coverageService.findCoverages(eq(10L))).thenReturn(sampleCoverageMap().get(10L));

        mockMvc.perform(
                        post("/v1/meritz/travel/plans/{planId}/coverages", 10L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                        {
                          "insurerId": 1,
                          "insBgnDt": "20260315",
                          "insEdDt": "20260320",
                          "trvArCd": "JP",
                          "representativeIndex": 0,
                          "insuredList": [
                            { "birth": "19900101", "gender": "1" }
                          ]
                        }
                        """))
                .andDo(print())
                .andDo(
                        document(
                                "meritz-travel-plan-coverages",
                                pathParameters(parameterWithName("planId").description("플랜 ID")),
                                requestFields(quoteRequestFields()),
                                responseFields(planCardResponseFields())));
    }

    @Test
    void silsonExclude() throws Exception {
        var silsonFamily = sampleFamilies().get(0);
        when(planService.findSilsonExcludeFamily(any(PlanCondition.class), eq(10L)))
                .thenReturn(silsonFamily);
        when(premiumService.calculateSingle(any(), any(), eq(0)))
                .thenReturn(samplePremiumMap().get(10L));
        when(coverageService.findCoverages(eq(10L))).thenReturn(sampleCoverageMap().get(10L));

        mockMvc.perform(
                        post("/v1/meritz/travel/plans/{planId}/silson-exclude", 10L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                        {
                          "insurerId": 1,
                          "insBgnDt": "20260315",
                          "insEdDt": "20260320",
                          "trvArCd": "JP",
                          "representativeIndex": 0,
                          "insuredList": [
                            { "birth": "19900101", "gender": "1" }
                          ]
                        }
                        """))
                .andDo(print())
                .andDo(
                        document(
                                "meritz-travel-silson-exclude",
                                pathParameters(parameterWithName("planId").description("플랜 ID")),
                                requestFields(quoteRequestFields()),
                                responseFields(planCardResponseFields())));
    }

    // ── Request Fields ──

    private static org.springframework.restdocs.payload.FieldDescriptor[] quoteRequestFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("insurerId").type(NUMBER).description("보험사 ID"),
            fieldWithPath("insBgnDt").type(STRING).description("보험시작일자 (YYYYMMDD)"),
            fieldWithPath("insEdDt").type(STRING).description("보험종료일자 (YYYYMMDD)"),
            fieldWithPath("trvArCd").type(STRING).description("여행지역코드"),
            fieldWithPath("representativeIndex").type(NUMBER).description("대표 피보험자 인덱스"),
            fieldWithPath("insuredList[].birth").type(STRING).description("피보험자 생년월일 (YYYYMMDD)"),
            fieldWithPath("insuredList[].gender").type(STRING).description("성별 (1: 남, 2: 여)"),
            fieldWithPath("silsonExclude")
                    .type(BOOLEAN)
                    .description("실손제외 여부 (true: 실손제외 플랜)")
                    .optional(),
        };
    }

    // ── Plan List Response Fields ──

    private static org.springframework.restdocs.payload.FieldDescriptor[] planListResponseFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("ok").type(BOOLEAN).description("성공 여부"),
            fieldWithPath("errCd").type(STRING).description("에러 코드").optional(),
            fieldWithPath("errMsg").type(STRING).description("에러 메시지").optional(),
            fieldWithPath("period.insBgnDt").type(STRING).description("보험시작일자"),
            fieldWithPath("period.insEdDt").type(STRING).description("보험종료일자"),
            fieldWithPath("insuredCount").type(NUMBER).description("피보험자 수"),
            fieldWithPath("plans[].planId").type(NUMBER).description("플랜 ID"),
            fieldWithPath("plans[].planGrpCd").type(STRING).description("플랜그룹코드"),
            fieldWithPath("plans[].planCd").type(STRING).description("플랜코드"),
            fieldWithPath("plans[].planNm").type(STRING).description("플랜명"),
            fieldWithPath("plans[].planNmRaw").type(STRING).description("플랜명 원본"),
            fieldWithPath("plans[].totalPremium").type(NUMBER).description("총 보험료"),
            fieldWithPath("plans[].currency").type(STRING).description("통화 (KRW)"),
            fieldWithPath("plans[].representativeCoverages[].covCd")
                    .type(STRING)
                    .description("대표 담보코드"),
            fieldWithPath("plans[].representativeCoverages[].covNm")
                    .type(STRING)
                    .description("대표 담보명"),
            fieldWithPath("plans[].representativeCoverages[].insdAmt")
                    .type(NUMBER)
                    .description("대표 보장금액"),
        };
    }

    // ── PlanCard Response Fields ──

    private static org.springframework.restdocs.payload.FieldDescriptor[] planCardResponseFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("planId").type(NUMBER).description("플랜 ID"),
            fieldWithPath("planGrpCd").type(STRING).description("플랜그룹코드"),
            fieldWithPath("planCd").type(STRING).description("플랜코드"),
            fieldWithPath("planNm").type(STRING).description("플랜명"),
            fieldWithPath("planNmRaw").type(STRING).description("플랜명 원본"),
            fieldWithPath("premium.ttPrem").type(NUMBER).description("총 보험료"),
            fieldWithPath("premium.currency").type(STRING).description("통화 (KRW)"),
            fieldWithPath("insuredPremiums[].index").type(NUMBER).description("피보험자 인덱스"),
            fieldWithPath("insuredPremiums[].currency").type(STRING).description("통화"),
            fieldWithPath("insuredPremiums[].ppsPrem").type(NUMBER).description("인당 보험료"),
            fieldWithPath("insuredPremiums[].birth").type(STRING).description("생년월일"),
            fieldWithPath("insuredPremiums[].gndrCd").type(STRING).description("성별코드"),
            fieldWithPath("insuredPremiums[].cusNm").type(STRING).description("고객명").optional(),
            fieldWithPath("insuredPremiums[].cusEngNm")
                    .type(STRING)
                    .description("고객영문명")
                    .optional(),
            fieldWithPath("insuredPremiums[].ageBandCode")
                    .type(STRING)
                    .description("연령대 코드")
                    .optional(),
            fieldWithPath("insuredPremiums[].ageBandLabel")
                    .type(STRING)
                    .description("연령대 라벨")
                    .optional(),
            fieldWithPath("coverageTitle").type(STRING).description("보장 타이틀").optional(),
            fieldWithPath("coverages[].covCd").type(STRING).description("담보코드"),
            fieldWithPath("coverages[].covNm").type(STRING).description("담보명"),
            fieldWithPath("coverages[].insdAmt").type(NUMBER).description("보장금액"),
            fieldWithPath("coverages[].cur").type(STRING).description("통화"),
            fieldWithPath("coverages[].categoryCode")
                    .type(STRING)
                    .description("카테고리 코드")
                    .optional(),
            fieldWithPath("coverages[].units[].ageBandCode").type(STRING).description("연령대 코드"),
            fieldWithPath("coverages[].units[].ageBandLabel").type(STRING).description("연령대 라벨"),
            fieldWithPath("coverages[].units[].count").type(NUMBER).description("인원수").optional(),
            fieldWithPath("coverages[].units[].insdAmt")
                    .type(NUMBER)
                    .description("보장금액")
                    .optional(),
            fieldWithPath("coverages[].units[].premSum")
                    .type(NUMBER)
                    .description("보험료 합")
                    .optional(),
        };
    }

    // ── Sample Data ──

    private List<PlanFamily> sampleFamilies() {
        TravelInsurancePlanEntity plan =
                TravelInsurancePlanEntity.builder()
                        .id(10L)
                        .planGroupCode("GRP01")
                        .planCode("TA2")
                        .planName("가뿐한플랜B_15~69세")
                        .planFullName("가뿐한플랜")
                        .productCode("PD001")
                        .unitProductCode("UPD001")
                        .ageGroupId(2)
                        .build();
        return List.of(new PlanFamily(1L, "가뿐한플랜B", plan, List.of(plan)));
    }

    private Map<Long, PremiumResult> samplePremiumMap() {
        var insuredPremiums =
                List.of(
                        new QuoteResult.InsuredPremium(
                                0, "KRW", 15000L, "19900101", "1", "홍길동", "HONG GILDONG"),
                        new QuoteResult.InsuredPremium(
                                1, "KRW", 12000L, "19920515", "2", "김영희", "KIM YOUNGHEE"));

        var coverageUnits =
                List.of(new QuoteResult.CoverageUnit("AGE_15_69", "15~69세", 2, 100000000L, 27000L));

        var coverageAmounts =
                Map.of(
                        "COV001", new QuoteResult.CoverageAmount(100000000L, "KRW", coverageUnits),
                        "COV002", new QuoteResult.CoverageAmount(30000000L, "KRW", coverageUnits));

        return Map.of(10L, new PremiumResult(27000L, insuredPremiums, coverageAmounts));
    }

    private Map<Long, List<QuoteResult.DbCoverage>> sampleCoverageMap() {
        return Map.of(
                10L,
                List.of(
                        new QuoteResult.DbCoverage("COV001", "해외상해사망", true, "DEATH"),
                        new QuoteResult.DbCoverage("COV002", "해외질병입원의료비", true, "MEDICAL")));
    }
}

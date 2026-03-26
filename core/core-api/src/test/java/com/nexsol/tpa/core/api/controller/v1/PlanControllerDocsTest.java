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

import com.nexsol.tpa.core.domain.coverage.Coverage;
import com.nexsol.tpa.core.domain.coverage.CoverageService;
import com.nexsol.tpa.core.domain.coverage.FamilyCoverageDetail;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.plan.PlanFamily;
import com.nexsol.tpa.core.domain.plan.PlanService;
import com.nexsol.tpa.core.domain.premium.*;
import com.nexsol.tpa.test.api.RestDocsTest;

@Tag("restdocs")
class PlanControllerDocsTest extends RestDocsTest {

    private final PlanService planService = mock(PlanService.class);
    private final CoverageService coverageService = mock(CoverageService.class);
    private final PremiumService premiumService = mock(PremiumService.class);

    @BeforeEach
    void setUpMockMvc(RestDocumentationContextProvider restDocumentation) {
        super.setUp(restDocumentation);
        this.mockMvc =
                mockController(new PlanController(planService, coverageService, premiumService));
    }

    @Test
    void plans() throws Exception {
        when(planService.findQuoteFamilies(any(PlanCondition.class))).thenReturn(sampleFamilies());
        when(premiumService.calculateAll(any(), any())).thenReturn(samplePremiumMap());
        when(coverageService.findCoveragesForFamilies(any())).thenReturn(sampleCoverageMap());
        when(planService.resolveSilsonExcludeMap(any(PlanCondition.class)))
                .thenReturn(Map.of(10L, 20L));

        mockMvc.perform(
                        post("/v1/travel/plans")
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
                                "travel-plans",
                                requestFields(quoteRequestFields()),
                                responseFields(planListResponseFields())));
    }

    @Test
    void planCoverages() throws Exception {
        var family = sampleFamilies().get(0);
        when(planService.findFamilyByPlanId(any(PlanCondition.class), eq(10L))).thenReturn(family);
        when(premiumService.calculateSingle(any(), any(), eq(0)))
                .thenReturn(samplePremiumMap().get(10L));
        when(coverageService.findCoverages(eq(1L))).thenReturn(sampleCoverageMap().get(1L));

        mockMvc.perform(
                        post("/v1/travel/plans/{planId}/coverages", 10L)
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
                                "travel-plan-coverages",
                                pathParameters(parameterWithName("planId").description("플랜 ID")),
                                requestFields(coverageRequestFields()),
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

    private static org.springframework.restdocs.payload.FieldDescriptor[] coverageRequestFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("insurerId").type(NUMBER).description("보험사 ID"),
            fieldWithPath("insBgnDt").type(STRING).description("보험시작일자 (YYYYMMDD)"),
            fieldWithPath("insEdDt").type(STRING).description("보험종료일자 (YYYYMMDD)"),
            fieldWithPath("trvArCd").type(STRING).description("여행지역코드"),
            fieldWithPath("representativeIndex").type(NUMBER).description("대표 피보험자 인덱스"),
            fieldWithPath("insuredList[].birth").type(STRING).description("피보험자 생년월일 (YYYYMMDD)"),
            fieldWithPath("insuredList[].gender").type(STRING).description("성별 (1: 남, 2: 여)"),
        };
    }

    // ── Plan List Response Fields ──

    private static org.springframework.restdocs.payload.FieldDescriptor[] planListResponseFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("result").type(STRING).description("결과"),
            fieldWithPath("data.period.insBgnDt").type(STRING).description("보험시작일자"),
            fieldWithPath("data.period.insEdDt").type(STRING).description("보험종료일자"),
            fieldWithPath("data.insuredCount").type(NUMBER).description("피보험자 수"),
            fieldWithPath("data.plans[].familyId").type(NUMBER).description("플랜 패밀리 ID"),
            fieldWithPath("data.plans[].planId").type(NUMBER).description("플랜 ID (대표플랜)"),
            fieldWithPath("data.plans[].planGrpCd").type(STRING).description("플랜그룹코드"),
            fieldWithPath("data.plans[].planCd").type(STRING).description("플랜코드"),
            fieldWithPath("data.plans[].planNm").type(STRING).description("플랜명"),
            fieldWithPath("data.plans[].planNmRaw").type(STRING).description("플랜명 원본"),
            fieldWithPath("data.plans[].silsonExcludePlanId")
                    .type(NUMBER)
                    .description("대응하는 실손제외 플랜 ID")
                    .optional(),
            fieldWithPath("data.plans[].totalPremium").type(NUMBER).description("총 보험료"),
            fieldWithPath("data.plans[].currency").type(STRING).description("통화 (KRW)"),
            fieldWithPath("data.plans[].representativeCoverages[].covCd")
                    .type(STRING)
                    .description("대표 담보코드"),
            fieldWithPath("data.plans[].representativeCoverages[].covNm")
                    .type(STRING)
                    .description("대표 담보 표시명"),
            fieldWithPath("data.plans[].representativeCoverages[].coverageName")
                    .type(STRING)
                    .description("대표 담보 원본명"),
            fieldWithPath("data.plans[].representativeCoverages[].insdAmt")
                    .type(NUMBER)
                    .description("대표 보장금액"),
            fieldWithPath("error").type(OBJECT).description("에러 정보").optional(),
        };
    }

    // ── PlanCard Response Fields ──

    private static org.springframework.restdocs.payload.FieldDescriptor[] planCardResponseFields() {
        return new org.springframework.restdocs.payload.FieldDescriptor[] {
            fieldWithPath("result").type(STRING).description("결과"),
            fieldWithPath("data.familyId").type(NUMBER).description("플랜 패밀리 ID"),
            fieldWithPath("data.planId").type(NUMBER).description("플랜 ID (대표플랜)"),
            fieldWithPath("data.planGrpCd").type(STRING).description("플랜그룹코드"),
            fieldWithPath("data.planCd").type(STRING).description("플랜코드"),
            fieldWithPath("data.planNm").type(STRING).description("플랜명"),
            fieldWithPath("data.planNmRaw").type(STRING).description("플랜명 원본"),
            fieldWithPath("data.premium.ttPrem").type(NUMBER).description("총 보험료"),
            fieldWithPath("data.premium.currency").type(STRING).description("통화 (KRW)"),
            fieldWithPath("data.insuredPremiums[].index").type(NUMBER).description("피보험자 인덱스"),
            fieldWithPath("data.insuredPremiums[].planId")
                    .type(NUMBER)
                    .description("피보험자별 플랜 ID")
                    .optional(),
            fieldWithPath("data.insuredPremiums[].currency").type(STRING).description("통화"),
            fieldWithPath("data.insuredPremiums[].ppsPrem").type(NUMBER).description("인당 보험료"),
            fieldWithPath("data.insuredPremiums[].birth").type(STRING).description("생년월일"),
            fieldWithPath("data.insuredPremiums[].gndrCd").type(STRING).description("성별코드"),
            fieldWithPath("data.insuredPremiums[].cusNm")
                    .type(STRING)
                    .description("고객명")
                    .optional(),
            fieldWithPath("data.insuredPremiums[].cusEngNm")
                    .type(STRING)
                    .description("고객영문명")
                    .optional(),
            fieldWithPath("data.insuredPremiums[].ageBandCode")
                    .type(STRING)
                    .description("연령대 코드")
                    .optional(),
            fieldWithPath("data.insuredPremiums[].ageBandLabel")
                    .type(STRING)
                    .description("연령대 라벨")
                    .optional(),
            fieldWithPath("data.coverageTitle").type(STRING).description("보장 타이틀").optional(),
            fieldWithPath("data.sections[].sectionCode").type(STRING).description("보장 섹션 코드"),
            fieldWithPath("data.sections[].sectionName").type(STRING).description("보장 섹션명"),
            fieldWithPath("data.sections[].coverages[].covCd").type(STRING).description("담보코드"),
            fieldWithPath("data.sections[].coverages[].covNm").type(STRING).description("담보 표시명"),
            fieldWithPath("data.sections[].coverages[].coverageName")
                    .type(STRING)
                    .description("담보 원본명"),
            fieldWithPath("data.sections[].coverages[].insdAmt").type(NUMBER).description("보장금액"),
            fieldWithPath("data.sections[].coverages[].cur").type(STRING).description("통화"),
            fieldWithPath("data.sections[].coverages[].units[].ageBandCode")
                    .type(STRING)
                    .description("연령대 코드"),
            fieldWithPath("data.sections[].coverages[].units[].ageBandLabel")
                    .type(STRING)
                    .description("연령대 라벨"),
            fieldWithPath("data.sections[].coverages[].units[].count")
                    .type(NUMBER)
                    .description("인원수")
                    .optional(),
            fieldWithPath("data.sections[].coverages[].units[].insdAmt")
                    .type(NUMBER)
                    .description("보장금액")
                    .optional(),
            fieldWithPath("data.sections[].coverages[].units[].premSum")
                    .type(NUMBER)
                    .description("보험료 합")
                    .optional(),
            fieldWithPath("error").type(OBJECT).description("에러 정보").optional(),
        };
    }

    // ── Sample Data ──

    private List<PlanFamily> sampleFamilies() {
        InsurancePlan plan =
                InsurancePlan.builder()
                        .id(10L)
                        .planGroupCode("GRP01")
                        .planCode("TA2")
                        .planName("가뿐한플랜B_15~69세")
                        .planFullName("가뿐한플랜")
                        .productCode("PD001")
                        .unitProductCode("UPD001")
                        .familyId(1L)
                        .ageGroupId(2)
                        .build();
        return List.of(
                PlanFamily.builder()
                        .familyId(1L)
                        .familyName("가뿐한플랜B")
                        .isLoss(true)
                        .repPlan(plan)
                        .plans(List.of(plan))
                        .build());
    }

    private Map<Long, Premium> samplePremiumMap() {
        var insuredPremiums =
                List.of(
                        new InsuredPremium(
                                0, "KRW", 15000L, "19900101", "1", "홍길동", "HONG GILDONG"),
                        new InsuredPremium(
                                1, "KRW", 12000L, "19920515", "2", "김영희", "KIM YOUNGHEE"));

        var coverageUnits = List.of(new CoverageUnit("AGE_15_69", "15~69세", 2, 100000000L, 27000L));

        var coverageAmounts =
                Map.of(
                        "COV001", new CoverageAmount(100000000L, "KRW", coverageUnits),
                        "COV002", new CoverageAmount(30000000L, "KRW", coverageUnits));

        return Map.of(10L, new Premium(27000L, insuredPremiums, coverageAmounts));
    }

    private Map<Long, List<FamilyCoverageDetail>> sampleCoverageMap() {
        Coverage cov1 =
                Coverage.builder()
                        .id(1L)
                        .insurerId(1L)
                        .coverageCode("COV001")
                        .coverageName("해외여행중상해_사망")
                        .sectionCode("DEATH_DISABILITY")
                        .build();
        Coverage cov2 =
                Coverage.builder()
                        .id(2L)
                        .insurerId(1L)
                        .coverageCode("COV002")
                        .coverageName("해외발생의료실비_상해")
                        .sectionCode("OVERSEAS_MEDICAL")
                        .build();

        return Map.of(
                1L,
                List.of(
                        FamilyCoverageDetail.builder()
                                .familyId(1L)
                                .coverage(cov1)
                                .sortOrder(1)
                                .featured(true)
                                .displayName("상해사망")
                                .sectionName("사망 또는 장해가 생겼을 때")
                                .sectionSortOrder(1)
                                .build(),
                        FamilyCoverageDetail.builder()
                                .familyId(1L)
                                .coverage(cov2)
                                .sortOrder(2)
                                .featured(true)
                                .displayName("해외상해의료비")
                                .sectionName("해외여행 중 다치거나 아팠을 때")
                                .sectionSortOrder(2)
                                .build()));
    }
}

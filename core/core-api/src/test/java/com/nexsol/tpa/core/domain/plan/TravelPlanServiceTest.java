package com.nexsol.tpa.core.domain.plan;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.nexsol.tpa.core.domain.premium.PlanCondition;
import com.nexsol.tpa.core.error.CoreException;

class TravelPlanServiceTest {

    private PlanReader planReader;
    private QuotePlanPolicy policy;
    private TravelPlanService service;

    @BeforeEach
    void setUp() {
        planReader = mock(PlanReader.class);
        policy = new QuotePlanPolicy();
        service = new TravelPlanService(planReader, policy);
    }

    // ── 테스트 데이터 ──

    private static InsurancePlan plan(Long id, int ageGroupId, String planCode, String planName) {
        return InsurancePlan.builder()
                .id(id)
                .familyId(1L)
                .planName(planName)
                .planFullName(planName)
                .planCode(planCode)
                .planGroupCode("TPA")
                .productCode("15540")
                .unitProductCode("15541")
                .ageGroupId(ageGroupId)
                .build();
    }

    private static PlanFamily family(
            Long familyId,
            String familyName,
            boolean isLoss,
            InsurancePlan repPlan,
            List<InsurancePlan> plans) {
        return PlanFamily.builder()
                .familyId(familyId)
                .familyName(familyName)
                .isLoss(isLoss)
                .repPlan(repPlan)
                .plans(plans)
                .build();
    }

    private static PlanCondition condition(
            String insBgnDt, List<PlanCondition.Insured> insuredList) {
        return new PlanCondition(1L, insBgnDt, "20260327", "", 0, insuredList, false);
    }

    private static PlanCondition.Insured insured(String birth, String gender) {
        return new PlanCondition.Insured(birth, gender);
    }

    private List<PlanFamily> allFamilies() {
        var planA_0_14 = plan(1L, 1, "TA11", "가뿐한플랜A_0~14세");
        var planA_15_69 = plan(7L, 2, "TA1", "가뿐한플랜A_15~69세");
        var planA_70_80 = plan(13L, 3, "TA12", "가뿐한플랜A_70~80세");

        var planB_0_14 = plan(2L, 1, "TA21", "가뿐한플랜B_0~14세");
        var planB_15_69 = plan(8L, 2, "TA2", "가뿐한플랜B_15~69세");

        var planA_15_69_ex = plan(20L, 2, "TA1P", "가뿐한플랜A_15~69세_실손제외");
        var planB_15_69_ex = plan(21L, 2, "TA2P", "가뿐한플랜B_15~69세_실손제외");

        return List.of(
                family(
                        7L,
                        "가뿐한플랜A",
                        true,
                        planA_15_69,
                        List.of(planA_0_14, planA_15_69, planA_70_80)),
                family(8L, "가뿐한플랜B", true, planB_15_69, List.of(planB_0_14, planB_15_69)),
                family(14L, "가뿐한플랜A 실손제외", false, planA_15_69_ex, List.of(planA_15_69_ex)),
                family(15L, "가뿐한플랜B 실손제외", false, planB_15_69_ex, List.of(planB_15_69_ex)));
    }

    private static void assertCoreException(Throwable t, String messageContains) {
        assertThat(t).isInstanceOf(CoreException.class);
        assertThat(t.getMessage()).contains(messageContains);
    }

    // ── 검증 실패 ──

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("insuredList가 비어있으면 예외")
        void emptyInsuredList() {
            var cmd = condition("20260326", List.of());

            assertThatThrownBy(() -> service.findQuoteFamilies(cmd))
                    .satisfies(t -> assertCoreException(t, "insuredList is empty"));
        }

        @Test
        @DisplayName("insurerId가 null이면 예외")
        void nullInsurerId() {
            var cmd =
                    new PlanCondition(
                            null,
                            "20260326",
                            "20260327",
                            "",
                            0,
                            List.of(insured("19941118", "1")),
                            false);

            assertThatThrownBy(() -> service.findQuoteFamilies(cmd))
                    .satisfies(t -> assertCoreException(t, "insurerId is required"));
        }

        @Test
        @DisplayName("representativeIndex가 범위 밖이면 예외")
        void invalidRepresentativeIndex() {
            var cmd =
                    new PlanCondition(
                            1L,
                            "20260326",
                            "20260327",
                            "",
                            5,
                            List.of(insured("19941118", "1")),
                            false);

            assertThatThrownBy(() -> service.findQuoteFamilies(cmd))
                    .satisfies(t -> assertCoreException(t, "representativeIndex is invalid"));
        }
    }

    // ── planType 결정 ──

    @Nested
    @DisplayName("findQuoteFamilies - planType 결정")
    class PlanType {

        @Test
        @DisplayName("모두 30세 → planType B, 실손포함 → 가뿐한플랜B")
        void allUnder70_typeB() {
            when(planReader.loadAllFamilies(1L)).thenReturn(allFamilies());

            var cmd =
                    condition(
                            "20260326",
                            List.of(insured("19941118", "1"), insured("19950101", "2")));

            List<PlanFamily> result = service.findQuoteFamilies(cmd);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).familyName()).isEqualTo("가뿐한플랜B");
        }

        @Test
        @DisplayName("70세 포함 → planType A, 실손포함 → 가뿐한플랜A")
        void has70_typeA() {
            when(planReader.loadAllFamilies(1L)).thenReturn(allFamilies());

            var cmd =
                    condition(
                            "20260326",
                            List.of(insured("19941118", "1"), insured("19550101", "1")));

            List<PlanFamily> result = service.findQuoteFamilies(cmd);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).familyName()).isEqualTo("가뿐한플랜A");
        }

        @Test
        @DisplayName("모두 70세 이상 → planType A")
        void all70Plus_typeA() {
            when(planReader.loadAllFamilies(1L)).thenReturn(allFamilies());

            var cmd =
                    condition(
                            "20260326",
                            List.of(insured("19550101", "1"), insured("19540101", "2")));

            List<PlanFamily> result = service.findQuoteFamilies(cmd);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).familyName()).isEqualTo("가뿐한플랜A");
        }

        @Test
        @DisplayName("실손제외 → 실손제외 패밀리만 반환")
        void silsonExclude() {
            when(planReader.loadAllFamilies(1L)).thenReturn(allFamilies());

            var cmd =
                    new PlanCondition(
                            1L,
                            "20260326",
                            "20260327",
                            "",
                            0,
                            List.of(insured("19941118", "1")),
                            true);

            List<PlanFamily> result = service.findQuoteFamilies(cmd);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).familyName()).isEqualTo("가뿐한플랜B 실손제외");
        }
    }

    // ── findFamilyByPlanId ──

    @Nested
    @DisplayName("findFamilyByPlanId")
    class FindFamilyByPlanId {

        @Test
        @DisplayName("planId가 해당 planType 패밀리에 속하면 성공")
        void found() {
            when(planReader.loadAllFamilies(1L)).thenReturn(allFamilies());

            var cmd = condition("20260326", List.of(insured("19941118", "1")));

            PlanFamily result = service.findFamilyByPlanId(cmd, 8L);

            assertThat(result.familyName()).isEqualTo("가뿐한플랜B");
        }

        @Test
        @DisplayName("planId가 다른 planType에 속하면 예외 — B플랜 id로 A타입 조회")
        void wrongPlanType() {
            when(planReader.loadAllFamilies(1L)).thenReturn(allFamilies());

            var cmd =
                    condition(
                            "20260326",
                            List.of(insured("19941118", "1"), insured("19550101", "1")));

            assertThatThrownBy(() -> service.findFamilyByPlanId(cmd, 8L))
                    .satisfies(t -> assertCoreException(t, "planId=8"));
        }

        @Test
        @DisplayName("존재하지 않는 planId면 예외")
        void notFound() {
            when(planReader.loadAllFamilies(1L)).thenReturn(allFamilies());

            var cmd = condition("20260326", List.of(insured("19941118", "1")));

            assertThatThrownBy(() -> service.findFamilyByPlanId(cmd, 999L))
                    .satisfies(t -> assertCoreException(t, "planId=999"));
        }
    }

    // ── findSilsonExcludePlanIdMap ──

    @Nested
    @DisplayName("findSilsonExcludePlanIdMap")
    class SilsonExcludeMap {

        @Test
        @DisplayName("실손포함 repPlanId → 실손제외 repPlanId 매핑")
        void mapping() {
            when(planReader.loadAllFamilies(1L)).thenReturn(allFamilies());

            var cmd = condition("20260326", List.of(insured("19941118", "1")));

            var map = service.findSilsonExcludePlanIdMap(cmd);

            assertThat(map).containsEntry(8L, 21L);
        }
    }

    // ── QuotePlanPolicy 단위 테스트 ──

    @Nested
    @DisplayName("QuotePlanPolicy")
    class PolicyTest {

        @Test
        @DisplayName("나이 계산 — 1994년생, 기준일 2026-03-26")
        void calcAge31() {
            assertThat(policy.calcAge("19941118", "20260326")).isEqualTo(31);
        }

        @Test
        @DisplayName("나이 계산 — 1955년생, 기준일 2026-03-26")
        void calcAge71() {
            assertThat(policy.calcAge("19550101", "20260326")).isEqualTo(71);
        }

        @Test
        @DisplayName("ageGroupId 매핑")
        void resolveAgeGroupId() {
            assertThat(policy.resolveAgeGroupId(5)).isEqualTo(1);
            assertThat(policy.resolveAgeGroupId(30)).isEqualTo(2);
            assertThat(policy.resolveAgeGroupId(70)).isEqualTo(3);
        }

        @Test
        @DisplayName("81세 이상 → ageGroupId null (가입 불가)")
        void resolveAgeGroupId_outOfRange() {
            assertThat(policy.resolveAgeGroupId(81)).isNull();
        }

        @Test
        @DisplayName("resolvePlanType — 오늘 기준 70세 포함이면 A")
        void resolvePlanType_A() {
            // 1950년생은 오늘 기준 확실히 70세 이상
            String type =
                    policy.resolvePlanType(
                            List.of(insured("19941118", "1"), insured("19500101", "1")));
            assertThat(type).isEqualTo("A");
        }

        @Test
        @DisplayName("resolvePlanType — 오늘 기준 모두 69세 이하면 B")
        void resolvePlanType_B() {
            String type =
                    policy.resolvePlanType(
                            List.of(insured("19941118", "1"), insured("19950101", "2")));
            assertThat(type).isEqualTo("B");
        }
    }
}

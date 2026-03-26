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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.nexsol.tpa.core.domain.contract.*;
import com.nexsol.tpa.core.domain.inquiry.InquiryService;
import com.nexsol.tpa.core.domain.inquiry.InsuredContractDetail;
import com.nexsol.tpa.core.domain.inquiry.InsuredContractSummary;
import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.domain.plan.InsurancePlan;
import com.nexsol.tpa.core.domain.refund.ContractRefund;
import com.nexsol.tpa.core.domain.refund.Refund;
import com.nexsol.tpa.core.domain.subscription.SubscriptionCommand;
import com.nexsol.tpa.core.domain.subscription.SubscriptionResult;
import com.nexsol.tpa.core.domain.subscription.SubscriptionService;
import com.nexsol.tpa.core.enums.TravelPaymentMethod;
import com.nexsol.tpa.core.support.PageResult;
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

    @Test
    void contractListByDb() throws Exception {
        var contract =
                ContractInfo.builder()
                        .id(100L)
                        .insurerId(1L)
                        .insurerName("메리츠화재")
                        .partnerId(1L)
                        .partnerName("TPA파트너")
                        .channelId(1L)
                        .channelName("채널1")
                        .policyNumber("POL001")
                        .quote(new Quote("QG001", "QR001"))
                        .totalPremium(BigDecimal.valueOf(27000))
                        .status("COMPLETED")
                        .applyDate(LocalDateTime.of(2026, 3, 15, 10, 0))
                        .insurePeriod(
                                new InsurePeriod(
                                        LocalDate.of(2026, 3, 15),
                                        LocalDate.of(2026, 3, 20),
                                        "JP",
                                        "일본"))
                        .auth(
                                AuthInfo.builder()
                                        .provider("NICE")
                                        .uniqueKey("UK001")
                                        .status("COMPLETED")
                                        .build())
                        .build();

        var payment =
                Payment.builder()
                        .id(1L)
                        .contractId(100L)
                        .paymentMethod("CARD")
                        .status("COMPLETED")
                        .paidAmount(BigDecimal.valueOf(27000))
                        .paymentDate(LocalDateTime.of(2026, 3, 15, 10, 5))
                        .build();

        var plan = InsurancePlan.builder().id(10L).planName("가뿐한플랜").planCode("PLAN_A").build();

        var people =
                List.of(
                        InsuredPerson.builder()
                                .id(1L)
                                .contractId(100L)
                                .planId(10L)
                                .isContractor(true)
                                .name("홍길동")
                                .englishName("HONG GILDONG")
                                .gender("1")
                                .residentNumber("9001011234567")
                                .passportNumber("M12345678")
                                .phone("01012345678")
                                .email("hong@test.com")
                                .insurePremium(BigDecimal.valueOf(27000))
                                .build());

        var detail =
                ContractDetail.builder()
                        .contract(contract)
                        .payment(payment)
                        .plan(plan)
                        .people(people)
                        .build();

        var pageResult = PageResult.of(List.of(detail), 1L, 20, 0);

        when(queryService.list(any(), eq(0), eq(20))).thenReturn(pageResult);

        mockMvc.perform(
                        get("/v1/travel/contracts")
                                .param("authUniqueKey", "UK001")
                                .param("page", "0")
                                .param("size", "20"))
                .andDo(print())
                .andDo(
                        document(
                                "contract-list-db",
                                queryParameters(
                                        parameterWithName("authUniqueKey")
                                                .description("본인인증 고유키")
                                                .optional(),
                                        parameterWithName("page")
                                                .description("페이지 번호 (기본값: 0)")
                                                .optional(),
                                        parameterWithName("size")
                                                .description("페이지 크기 (기본값: 20)")
                                                .optional()),
                                responseFields(bridgeResponseFields())));
    }

    @Test
    void contractDetail() throws Exception {
        var detail =
                ContractDetail.builder()
                        .contract(
                                ContractInfo.builder()
                                        .id(100L)
                                        .insurerId(1L)
                                        .insurerName("메리츠화재")
                                        .partnerId(1L)
                                        .partnerName("TPA파트너")
                                        .channelId(1L)
                                        .channelName("채널1")
                                        .policyNumber("POL001")
                                        .quote(new Quote("QG001", "QR001"))
                                        .totalPremium(BigDecimal.valueOf(27000))
                                        .status("COMPLETED")
                                        .applyDate(LocalDateTime.of(2026, 3, 15, 10, 0))
                                        .insurePeriod(
                                                new InsurePeriod(
                                                        LocalDate.of(2026, 3, 15),
                                                        LocalDate.of(2026, 3, 20),
                                                        "JP",
                                                        "일본"))
                                        .build())
                        .payment(
                                Payment.builder()
                                        .id(1L)
                                        .contractId(100L)
                                        .paymentMethod("CARD")
                                        .status("CANCELED")
                                        .paidAmount(BigDecimal.valueOf(27000))
                                        .paymentDate(LocalDateTime.of(2026, 3, 15, 10, 5))
                                        .cancelDate(LocalDateTime.of(2026, 3, 16, 14, 0))
                                        .build())
                        .plan(
                                InsurancePlan.builder()
                                        .id(10L)
                                        .planName("가뿐한플랜")
                                        .planCode("PLAN_A")
                                        .build())
                        .refund(
                                Refund.builder()
                                        .id(1L)
                                        .contractId(100L)
                                        .paymentId(1L)
                                        .refundAmount(BigDecimal.valueOf(27000))
                                        .refundMethod(TravelPaymentMethod.CARD)
                                        .refundReason("단순 변심")
                                        .refundedAt(LocalDateTime.of(2026, 3, 16, 14, 0))
                                        .build())
                        .people(
                                List.of(
                                        InsuredPerson.builder()
                                                .id(1L)
                                                .planId(10L)
                                                .isContractor(true)
                                                .name("홍길동")
                                                .englishName("HONG GILDONG")
                                                .gender("1")
                                                .residentNumber("9001011234567")
                                                .passportNumber("M12345678")
                                                .phone("01012345678")
                                                .email("hong@test.com")
                                                .insurePremium(BigDecimal.valueOf(27000))
                                                .build()))
                        .build();

        when(queryService.get(100L)).thenReturn(detail);

        mockMvc.perform(get("/v1/travel/contracts/{id}", 100L))
                .andDo(print())
                .andDo(
                        document(
                                "contract-detail",
                                pathParameters(parameterWithName("id").description("계약 ID")),
                                responseFields(contractDetailResponseFields())));
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
            contractDetailResponseFields() {
        return apiResponseFields(
                fieldWithPath("data.contractId").type(NUMBER).description("계약 ID"),
                fieldWithPath("data.policyNumber").type(STRING).description("증권번호").optional(),
                fieldWithPath("data.totalPremium").type(NUMBER).description("총 보험료"),
                fieldWithPath("data.status").type(STRING).description("계약 상태"),
                fieldWithPath("data.applyDate").type(STRING).description("신청일시"),
                fieldWithPath("data.termsUrl").type(STRING).description("약관 URL"),
                fieldWithPath("data.policyLink").type(STRING).description("증권 링크").optional(),
                fieldWithPath("data.insureStartDate").type(STRING).description("보험 시작일"),
                fieldWithPath("data.insureEndDate").type(STRING).description("보험 종료일"),
                fieldWithPath("data.countryCode").type(STRING).description("국가코드"),
                fieldWithPath("data.countryName").type(STRING).description("국가명"),
                fieldWithPath("data.quoteGroupNumber")
                        .type(STRING)
                        .description("견적 그룹번호")
                        .optional(),
                fieldWithPath("data.quoteRequestNumber")
                        .type(STRING)
                        .description("견적 요청번호")
                        .optional(),
                fieldWithPath("data.insurerId").type(NUMBER).description("보험사 ID"),
                fieldWithPath("data.insurerName").type(STRING).description("보험사명"),
                fieldWithPath("data.partnerId").type(NUMBER).description("제휴사 ID"),
                fieldWithPath("data.partnerName").type(STRING).description("제휴사명"),
                fieldWithPath("data.channelId").type(NUMBER).description("채널 ID"),
                fieldWithPath("data.channelName").type(STRING).description("채널명"),
                fieldWithPath("data.planId").type(NUMBER).description("플랜 ID"),
                fieldWithPath("data.planName").type(STRING).description("플랜명"),
                fieldWithPath("data.planCode").type(STRING).description("플랜코드"),
                fieldWithPath("data.paymentMethod").type(STRING).description("결제수단"),
                fieldWithPath("data.paymentStatus").type(STRING).description("결제 상태"),
                fieldWithPath("data.paidAmount").type(NUMBER).description("결제 금액"),
                fieldWithPath("data.paymentDate").type(STRING).description("결제일시"),
                fieldWithPath("data.refund").type(OBJECT).description("환불 정보 (임의해지 시)").optional(),
                fieldWithPath("data.refund.refundAmount")
                        .type(NUMBER)
                        .description("환불 금액")
                        .optional(),
                fieldWithPath("data.refund.refundMethod")
                        .type(STRING)
                        .description("환불 수단")
                        .optional(),
                fieldWithPath("data.refund.bankName").type(STRING).description("은행명").optional(),
                fieldWithPath("data.refund.accountNumber")
                        .type(STRING)
                        .description("계좌번호")
                        .optional(),
                fieldWithPath("data.refund.depositorName")
                        .type(STRING)
                        .description("예금주명")
                        .optional(),
                fieldWithPath("data.refund.refundReason")
                        .type(STRING)
                        .description("환불 사유")
                        .optional(),
                fieldWithPath("data.refund.refundedAt").type(STRING).description("환불일시").optional(),
                fieldWithPath("data.people").type(ARRAY).description("피보험자 목록"),
                fieldWithPath("data.people[].id").type(NUMBER).description("피보험자 ID"),
                fieldWithPath("data.people[].planId").type(NUMBER).description("플랜 ID"),
                fieldWithPath("data.people[].isContractor").type(BOOLEAN).description("계약자 여부"),
                fieldWithPath("data.people[].name").type(STRING).description("이름"),
                fieldWithPath("data.people[].englishName").type(STRING).description("영문이름"),
                fieldWithPath("data.people[].gender").type(STRING).description("성별"),
                fieldWithPath("data.people[].residentNumberMasked")
                        .type(STRING)
                        .description("주민번호 (마스킹)"),
                fieldWithPath("data.people[].passportNumberMasked")
                        .type(STRING)
                        .description("여권번호 (마스킹)"),
                fieldWithPath("data.people[].phone").type(STRING).description("연락처"),
                fieldWithPath("data.people[].email").type(STRING).description("이메일"),
                fieldWithPath("data.people[].insurePremium").type(NUMBER).description("개인 보험료"));
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

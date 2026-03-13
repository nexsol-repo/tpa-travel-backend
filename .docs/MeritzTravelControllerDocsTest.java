package com.nexsol.tpa.meritzbridge.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nexsol.tpa.meritzbridge.dto.response.BridgeResponse;
import com.nexsol.tpa.meritzbridge.service.MeritzBridgeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

@ExtendWith(RestDocumentationExtension.class)
@WebFluxTest(MeritzTravelController.class)
@AutoConfigureRestDocs
class MeritzTravelControllerDocsTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private MeritzBridgeService service;

	private final ObjectMapper om = new ObjectMapper();

	private BridgeResponse mockOkResponse() {
		ObjectNode data = om.createObjectNode();
		data.put("resultField", "sampleValue");
		return BridgeResponse.ok("00000", data);
	}

	private void stubService(MeritzApi api) {
		when(service.callMeritzApi(eq(api), any())).thenReturn(Mono.just(mockOkResponse()));
	}

	/** 공통 필드가 포함된 base ObjectNode 생성 */
	private ObjectNode baseJson() {
		ObjectNode node = om.createObjectNode();
		node.put("company", "TPA");
		node.put("gnrAflcoCd", "GA001");
		node.put("aflcoDivCd", "AD01");
		node.put("bizpeNo", "1234567890");
		return node;
	}

	// --- 공통 필드 디스크립터 ---

	private static org.springframework.restdocs.payload.FieldDescriptor[] commonRequestFields() {
		return new org.springframework.restdocs.payload.FieldDescriptor[]{
				fieldWithPath("company").description("회사 구분 (TPA / INSBOON)"),
				fieldWithPath("gnrAflcoCd").description("일반제휴사코드").optional(),
				fieldWithPath("aflcoDivCd").description("제휴사구분코드").optional(),
				fieldWithPath("bizpeNo").description("사업자번호").optional()
		};
	}

	private static org.springframework.restdocs.payload.FieldDescriptor[] commonResponseFields() {
		return new org.springframework.restdocs.payload.FieldDescriptor[]{
				fieldWithPath("success").type(BOOLEAN).description("성공 여부"),
				fieldWithPath("errCd").type(STRING).description("에러 코드 (성공 시 00000)").optional(),
				fieldWithPath("errMsg").type(STRING).description("에러 메시지").optional(),
				subsectionWithPath("data").type(OBJECT).description("Meritz API 응답 데이터 (JSON 패스스루)").optional()
		};
	}

	private void doPost(String uri, ObjectNode body, String docId,
						org.springframework.restdocs.payload.FieldDescriptor... extraRequestFields) {
		var allFields = new org.springframework.restdocs.payload.FieldDescriptor[commonRequestFields().length + extraRequestFields.length];
		System.arraycopy(commonRequestFields(), 0, allFields, 0, commonRequestFields().length);
		System.arraycopy(extraRequestFields, 0, allFields, commonRequestFields().length, extraRequestFields.length);

		webTestClient.post().uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body.toString())
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.consumeWith(document(docId,
						requestFields(allFields),
						responseFields(commonResponseFields())
				));
	}

	// =========================================================================
	// 1. 플랜조회
	// =========================================================================
	@Test
	void planInquiry() {
		stubService(MeritzApi.PLAN_INQ);
		ObjectNode body = baseJson();
		body.put("polNo", "POL001");
		body.put("stdDt", "20250101");

		doPost("/internal/meritz/plan-inquiry", body, "plan-inquiry",
				fieldWithPath("polNo").description("증권번호"),
				fieldWithPath("stdDt").description("기준일자 (YYYYMMDD)")
		);
	}

	// =========================================================================
	// 2. 도시/국가코드 조회
	// =========================================================================
	@Test
	void cityCountryCode() {
		stubService(MeritzApi.CITY_NATL_CD_INQ);
		ObjectNode body = baseJson();
		body.put("srchVal", "JAPAN");
		body.put("srchCnd", "1");

		doPost("/internal/meritz/city-country-code", body, "city-country-code",
				fieldWithPath("srchVal").description("검색값"),
				fieldWithPath("srchCnd").description("검색조건")
		);
	}

	// =========================================================================
	// 3. 보험료 산출
	// =========================================================================
	@Test
	void premiumCalculate() {
		stubService(MeritzApi.HNDY_PREM_CMPT);
		ObjectNode body = baseJson();
		body.put("polNo", "POL001");
		body.put("pdCd", "PD01");
		body.put("untPdCd", "UPD01");
		body.put("sbcpDt", "20250101");
		body.put("insBgnDt", "20250110");
		body.put("insEdDt", "20250120");
		body.put("trvArCd", "JP");
		body.put("inspeCnt", "1");

		ArrayNode inspeList = body.putArray("opapiTrvPremCmptInspeInfCbcVo");
		ObjectNode inspe = inspeList.addObject();
		inspe.put("planGrpCd", "GRP01");
		inspe.put("planCd", "PLAN01");
		inspe.put("inspeRsidNo", "9901011234567");
		inspe.put("inspeBdt", "19990101");
		inspe.put("gndrCd", "M");
		inspe.put("inspeNm", "홍길동");
		inspe.put("engInspeNm", "HONG GILDONG");

		doPost("/internal/meritz/premium-calculate", body, "premium-calculate",
				fieldWithPath("polNo").description("증권번호"),
				fieldWithPath("pdCd").description("상품코드"),
				fieldWithPath("untPdCd").description("단위상품코드"),
				fieldWithPath("sbcpDt").description("청약일자"),
				fieldWithPath("insBgnDt").description("보험시작일자"),
				fieldWithPath("insEdDt").description("보험종료일자"),
				fieldWithPath("trvArCd").description("여행지역코드"),
				fieldWithPath("inspeCnt").description("피보험자수"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].planGrpCd").description("플랜그룹코드"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].planCd").description("플랜코드"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].inspeRsidNo").description("피보험자주민번호"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].inspeBdt").description("피보험자생년월일"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].gndrCd").description("성별코드"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].inspeNm").description("피보험자명"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].engInspeNm").description("피보험자영문명")
		);
	}

	// =========================================================================
	// 4. 견적 저장
	// =========================================================================
	@Test
	void estimateSave() {
		stubService(MeritzApi.EST_SAVE);
		ObjectNode body = baseJson();
		body.put("polNo", "POL001");
		body.put("pdCd", "PD01");
		body.put("untPdCd", "UPD01");
		body.put("sbcpDt", "20250101");
		body.put("insBgnDt", "20250110");
		body.put("insEdDt", "20250120");
		body.put("trvArCd", "JP");
		body.put("inspeCnt", "1");
		body.put("grupSalChnDivCd", "01");

		ArrayNode inspeList = body.putArray("opapiTrvPremCmptInspeInfCbcVo");
		ObjectNode inspe = inspeList.addObject();
		inspe.put("planGrpCd", "GRP01");
		inspe.put("planCd", "PLAN01");
		inspe.put("inspeRsidNo", "9901011234567");
		inspe.put("inspeBdt", "19990101");
		inspe.put("gndrCd", "M");
		inspe.put("inspeNm", "홍길동");
		inspe.put("engInspeNm", "HONG GILDONG");

		ObjectNode ctr = body.putObject("ctrTrsInfBcVo");
		ctr.put("crdNo", "4111111111111111");
		ctr.put("efctPrd", "2512");
		ctr.put("dporNm", "홍길동");
		ctr.put("dporCd", "01");

		doPost("/internal/meritz/estimate-save", body, "estimate-save",
				fieldWithPath("polNo").description("증권번호"),
				fieldWithPath("pdCd").description("상품코드"),
				fieldWithPath("untPdCd").description("단위상품코드"),
				fieldWithPath("sbcpDt").description("청약일자"),
				fieldWithPath("insBgnDt").description("보험시작일자"),
				fieldWithPath("insEdDt").description("보험종료일자"),
				fieldWithPath("trvArCd").description("여행지역코드"),
				fieldWithPath("inspeCnt").description("피보험자수"),
				fieldWithPath("grupSalChnDivCd").description("그룹판매채널구분코드"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].planGrpCd").description("플랜그룹코드"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].planCd").description("플랜코드"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].inspeRsidNo").description("피보험자주민번호"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].inspeBdt").description("피보험자생년월일"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].gndrCd").description("성별코드"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].inspeNm").description("피보험자명"),
				fieldWithPath("opapiTrvPremCmptInspeInfCbcVo[].engInspeNm").description("피보험자영문명"),
				fieldWithPath("ctrTrsInfBcVo.crdNo").description("카드번호"),
				fieldWithPath("ctrTrsInfBcVo.efctPrd").description("유효기간"),
				fieldWithPath("ctrTrsInfBcVo.dporNm").description("예금주명"),
				fieldWithPath("ctrTrsInfBcVo.dporCd").description("예금주코드")
		);
	}

	// =========================================================================
	// 5. 계약 목록 조회
	// =========================================================================
	@Test
	void contractList() {
		stubService(MeritzApi.CTR_LST_INQ);
		ObjectNode body = baseJson();
		body.put("polNo", "POL001");
		body.put("inqStDt", "20250101");
		body.put("inqEdDt", "20250131");

		doPost("/internal/meritz/contract-list", body, "contract-list",
				fieldWithPath("polNo").description("증권번호"),
				fieldWithPath("inqStDt").description("조회시작일자 (YYYYMMDD)"),
				fieldWithPath("inqEdDt").description("조회종료일자 (YYYYMMDD)")
		);
	}

	// =========================================================================
	// 6. 가입증명서
	// =========================================================================
	@Test
	void certificate() {
		stubService(MeritzApi.SBC_CTF_OTPT);
		ObjectNode body = baseJson();
		body.put("polNo", "POL001");
		body.put("pdCd", "PD01");
		body.put("quotGrpNo", "QG001");
		body.put("quotReqNo", "QR001");
		body.put("otptDiv", "1");
		body.put("otptTpCd", "01");

		doPost("/internal/meritz/certificate", body, "certificate",
				fieldWithPath("polNo").description("증권번호"),
				fieldWithPath("pdCd").description("상품코드"),
				fieldWithPath("quotGrpNo").description("견적그룹번호"),
				fieldWithPath("quotReqNo").description("견적요청번호"),
				fieldWithPath("otptDiv").description("출력구분"),
				fieldWithPath("otptTpCd").description("출력유형코드")
		);
	}

	// =========================================================================
	// 7. 추천플랜 조회
	// =========================================================================
	@Test
	void recommendPlan() {
		stubService(MeritzApi.TRV_RCM_PLAN_INQ);
		ObjectNode body = baseJson();
		body.put("cityNatlCd", "JP");
		body.put("age", "30");
		body.put("gndrCd", "M");
		body.put("chdnAcmpYn", "N");

		doPost("/internal/meritz/recommend-plan", body, "recommend-plan",
				fieldWithPath("cityNatlCd").description("도시/국가코드"),
				fieldWithPath("age").description("나이"),
				fieldWithPath("gndrCd").description("성별코드 (M/F)"),
				fieldWithPath("chdnAcmpYn").description("자녀동반여부 (Y/N)")
		);
	}

	// =========================================================================
	// 8. 계약 취소
	// =========================================================================
	@Test
	void contractCancel() {
		stubService(MeritzApi.TRV_CHANGE_CTR);
		ObjectNode body = baseJson();
		body.put("polNo", "POL001");
		body.put("quotGrpNo", "QG001");
		body.put("quotReqNo", "QR001");

		doPost("/internal/meritz/contract-cancel", body, "contract-cancel",
				fieldWithPath("polNo").description("증권번호"),
				fieldWithPath("quotGrpNo").description("견적그룹번호"),
				fieldWithPath("quotReqNo").description("견적요청번호")
		);
	}

	// =========================================================================
	// 9. 정산 목록 조회
	// =========================================================================
	@Test
	void settlementList() {
		stubService(MeritzApi.ADJT_LST_INQ);
		ObjectNode body = baseJson();
		body.put("polNo", "POL001");
		body.put("bizDivCd", "01");
		body.put("inqStDt", "20250101");
		body.put("inqEdDt", "20250131");

		doPost("/internal/meritz/settlement-list", body, "settlement-list",
				fieldWithPath("polNo").description("증권번호"),
				fieldWithPath("bizDivCd").description("사업구분코드"),
				fieldWithPath("inqStDt").description("조회시작일자 (YYYYMMDD)"),
				fieldWithPath("inqEdDt").description("조회종료일자 (YYYYMMDD)")
		);
	}

	// =========================================================================
	// 10. 고객전환
	// =========================================================================
	@Test
	void customerConvert() {
		stubService(MeritzApi.TRV_ITGR_CUS_CONV);
		ObjectNode body = baseJson();
		body.put("polNo", "POL001");
		body.put("quotGrpNo", "QG001");
		body.put("quotReqNo", "QR001");
		body.put("inspeRsidNo", "9901011234567");
		body.put("inspeNm", "홍길동");

		doPost("/internal/meritz/customer-convert", body, "customer-convert",
				fieldWithPath("polNo").description("증권번호"),
				fieldWithPath("quotGrpNo").description("견적그룹번호"),
				fieldWithPath("quotReqNo").description("견적요청번호"),
				fieldWithPath("inspeRsidNo").description("피보험자주민번호"),
				fieldWithPath("inspeNm").description("피보험자명")
		);
	}

	// =========================================================================
	// 11. 배서보험료 산출
	// =========================================================================
	@Test
	void endorsementPremium() {
		stubService(MeritzApi.CALC_ENDR_PREM);
		ObjectNode body = baseJson();
		body.put("polNo", "POL001");
		body.put("quotGrpNo", "QG001");
		body.put("quotReqNo", "QR001");
		body.put("endrStdDt", "20250115");
		body.put("chgInsEdDt", "20250125");

		doPost("/internal/meritz/endorsement-premium", body, "endorsement-premium",
				fieldWithPath("polNo").description("증권번호"),
				fieldWithPath("quotGrpNo").description("견적그룹번호"),
				fieldWithPath("quotReqNo").description("견적요청번호"),
				fieldWithPath("endrStdDt").description("배서기준일자 (YYYYMMDD)"),
				fieldWithPath("chgInsEdDt").description("변경보험종료일자 (YYYYMMDD)")
		);
	}

	// =========================================================================
	// 12. 배서체결 저장
	// =========================================================================
	@Test
	void endorsementSave() {
		stubService(MeritzApi.SAVE_ENDR_CCLU);
		ObjectNode body = baseJson();
		body.put("polNo", "POL001");
		body.put("quotGrpNo", "QG001");
		body.put("quotReqNo", "QR001");
		body.put("endrStdDt", "20250115");
		body.put("chgInsEdDt", "20250125");

		doPost("/internal/meritz/endorsement-save", body, "endorsement-save",
				fieldWithPath("polNo").description("증권번호"),
				fieldWithPath("quotGrpNo").description("견적그룹번호"),
				fieldWithPath("quotReqNo").description("견적요청번호"),
				fieldWithPath("endrStdDt").description("배서기준일자 (YYYYMMDD)"),
				fieldWithPath("chgInsEdDt").description("변경보험종료일자 (YYYYMMDD)")
		);
	}

	// =========================================================================
	// 13. 카드승인
	// =========================================================================
	@Test
	void cardApprove() {
		stubService(MeritzApi.CRD_APV);
		ObjectNode body = baseJson();
		body.put("polNo", "POL001");
		body.put("quotGrpNo", "QG001");
		body.put("quotReqNo", "QR001");
		body.put("rcptPrem", "50000");

		ObjectNode ctr = body.putObject("ctrTrsInfBcVo");
		ctr.put("crdNo", "4111111111111111");
		ctr.put("efctPrd", "2512");
		ctr.put("dporNm", "홍길동");
		ctr.put("dporCd", "01");

		ArrayNode quotList = body.putArray("opapiGrupTrvQuotInfCbcVo");
		ObjectNode quot = quotList.addObject();
		quot.put("quotGrpNo", "QG001");
		quot.put("quotReqNo", "QR001");

		doPost("/internal/meritz/card-approve", body, "card-approve",
				fieldWithPath("polNo").description("증권번호"),
				fieldWithPath("quotGrpNo").description("견적그룹번호"),
				fieldWithPath("quotReqNo").description("견적요청번호"),
				fieldWithPath("rcptPrem").description("영수보험료"),
				fieldWithPath("ctrTrsInfBcVo.crdNo").description("카드번호"),
				fieldWithPath("ctrTrsInfBcVo.efctPrd").description("유효기간"),
				fieldWithPath("ctrTrsInfBcVo.dporNm").description("예금주명"),
				fieldWithPath("ctrTrsInfBcVo.dporCd").description("예금주코드"),
				fieldWithPath("opapiGrupTrvQuotInfCbcVo[].quotGrpNo").description("견적그룹번호"),
				fieldWithPath("opapiGrupTrvQuotInfCbcVo[].quotReqNo").description("견적요청번호")
		);
	}

	// =========================================================================
	// 14. 계약 상세 조회
	// =========================================================================
	@Test
	void contractDetail() {
		stubService(MeritzApi.TRV_CTR_INQ);
		ObjectNode body = baseJson();
		body.put("polNo", "POL001");
		body.put("quotGrpNo", "QG001");
		body.put("quotReqNo", "QR001");

		doPost("/internal/meritz/contract-detail", body, "contract-detail",
				fieldWithPath("polNo").description("증권번호"),
				fieldWithPath("quotGrpNo").description("견적그룹번호"),
				fieldWithPath("quotReqNo").description("견적요청번호")
		);
	}

	// =========================================================================
	// 15. 카드취소
	// =========================================================================
	@Test
	void cardCancel() {
		stubService(MeritzApi.CRD_CNC);
		ObjectNode body = baseJson();
		body.put("polNo", "POL001");
		body.put("quotGrpNo", "QG001");
		body.put("quotReqNo", "QR001");

		doPost("/internal/meritz/card-cancel", body, "card-cancel",
				fieldWithPath("polNo").description("증권번호"),
				fieldWithPath("quotGrpNo").description("견적그룹번호"),
				fieldWithPath("quotReqNo").description("견적요청번호")
		);
	}
}

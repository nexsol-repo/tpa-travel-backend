package com.nexsol.tpa.provider.meritz.inquiry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.client.InquiryProvider;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient;
import com.nexsol.tpa.core.domain.client.InsuranceContractClient.BridgeApiResult;
import com.nexsol.tpa.core.domain.inquiry.InsuredContractDetail;
import com.nexsol.tpa.core.domain.inquiry.InsuredContractSummary;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeritzInquiryProvider implements InquiryProvider {

    private final InsuranceContractClient contractClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<InsuredContractSummary> contractList(
            String company, Map<String, Object> bodyFields) {
        BridgeApiResult res = contractClient.contractList(company, bodyFields);
        if (!res.success()) {
            throw new CoreException(
                    CoreErrorType.DEFAULT_ERROR,
                    "계약목록조회 실패. errCd=" + res.errCd() + ", errMsg=" + res.errMsg());
        }
        return parseContractList(res.data());
    }

    @Override
    public InsuredContractDetail contractDetail(String company, Map<String, Object> bodyFields) {
        BridgeApiResult res = contractClient.contractDetail(company, bodyFields);
        if (!res.success()) {
            throw new CoreException(
                    CoreErrorType.DEFAULT_ERROR,
                    "계약조회 실패. errCd=" + res.errCd() + ", errMsg=" + res.errMsg());
        }
        return parseContractDetail(res.data());
    }

    private InsuredContractDetail parseContractDetail(Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            JsonNode node = objectMapper.readTree(json);

            return InsuredContractDetail.builder()
                    .polNo(node.path("polNo").asText(null))
                    .quotGrpNo(node.path("quotGrpNo").asText(null))
                    .quotReqNo(node.path("quotReqNo").asText(null))
                    .sbcpDt(node.path("sbcpDt").asText(null))
                    .insBgnDt(node.path("insBgnDt").asText(null))
                    .insEdDt(node.path("insEdDt").asText(null))
                    .pdNm(node.path("pdNm").asText(null))
                    .pdCd(node.path("pdCd").asText(null))
                    .ttPrem(node.path("ttPrem").asText(null))
                    .stat(node.path("stat").asText(null))
                    .crdApvNo(node.path("crdApvNo").asText(null))
                    .crdCncDt(node.path("crdCncDt").asText(null))
                    .adjtYn(node.path("adjtYn").asText(null))
                    .build();
        } catch (Exception e) {
            log.error("[MERITZ_INQUIRY] 계약상세 파싱 실패", e);
            throw new CoreException(CoreErrorType.DEFAULT_ERROR, "계약상세 응답 파싱 실패");
        }
    }

    private List<InsuredContractSummary> parseContractList(Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            JsonNode node = objectMapper.readTree(json);
            JsonNode listNode = node.path("opapiGnrCoprCtrLstInfCbcVo");

            if (listNode.isMissingNode() || !listNode.isArray()) {
                return List.of();
            }

            List<InsuredContractSummary> result = new ArrayList<>();
            for (JsonNode item : listNode) {
                result.add(
                        InsuredContractSummary.builder()
                                .polNo(item.path("polNo").asText(null))
                                .quotGrpNo(item.path("quotGrpNo").asText(null))
                                .quotReqNo(item.path("quotReqNo").asText(null))
                                .sbcpDt(item.path("sbcpDt").asText(null))
                                .insBgnDt(item.path("insBgnDt").asText(null))
                                .insEdDt(item.path("insEdDt").asText(null))
                                .pdNm(item.path("pdNm").asText(null))
                                .pdCd(item.path("pdCd").asText(null))
                                .inspeNm(item.path("inspeNm").asText(null))
                                .ttInspeNum(
                                        item.path("ttInspeNum").isNull()
                                                ? null
                                                : item.path("ttInspeNum").asInt())
                                .ttPrem(item.path("ttPrem").asText(null))
                                .stat(item.path("stat").asText(null))
                                .build());
            }
            return result;
        } catch (Exception e) {
            log.error("[MERITZ_INQUIRY] 계약목록 파싱 실패", e);
            throw new CoreException(CoreErrorType.DEFAULT_ERROR, "계약목록 응답 파싱 실패");
        }
    }
}

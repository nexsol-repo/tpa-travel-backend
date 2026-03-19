package com.nexsol.tpa.core.domain.cancel;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.nexsol.tpa.client.meritz.contract.MeritzContractClient;
import com.nexsol.tpa.core.domain.contract.ContractReader;
import com.nexsol.tpa.core.domain.contract.ContractValidator;
import com.nexsol.tpa.core.domain.payment.PaymentReader;
import com.nexsol.tpa.core.domain.payment.PaymentWriter;
import com.nexsol.tpa.core.domain.plan.PlanReader;
import com.nexsol.tpa.core.domain.refund.RefundCommand;
import com.nexsol.tpa.core.domain.refund.RefundWriter;
import com.nexsol.tpa.core.domain.snapshot.SnapshotAppender;
import com.nexsol.tpa.core.enums.TravelPaymentMethod;
import com.nexsol.tpa.core.enums.TravelPaymentStatus;
import com.nexsol.tpa.storage.db.core.entity.TravelContractEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurancePlanEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurePaymentEntity;
import com.nexsol.tpa.storage.db.core.entity.TravelInsurerEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelService {

    private final MeritzContractClient meritzClient;
    private final ContractReader contractReader;
    private final ContractValidator contractValidator;
    private final PaymentReader paymentReader;
    private final PaymentWriter paymentWriter;
    private final RefundWriter refundWriter;
    private final PlanReader planReader;
    private final SnapshotAppender snapshotAppender;
    private final ObjectMapper objectMapper;

    @Transactional
    public CancelResult cancel(String company, RefundCommand refundCommand) {
        Long contractId = refundCommand.contractId();
        TravelContractEntity contract = contractReader.getById(contractId);
        TravelInsurePaymentEntity payment = paymentReader.getByContractId(contract.getId());
        TravelInsurancePlanEntity plan = planReader.getById(contract.getPlanId());
        TravelInsurerEntity insurer = planReader.getInsurerById(plan.getInsurerId());

        // 이미 취소 완료면 결과 즉시 반환
        if (payment.getStatus() == TravelPaymentStatus.CANCELED) {
            return buildResult(contract, payment, plan, insurer);
        }

        contractValidator.requireNotCanceled(payment);

        ContractValidator.requireNotBlank(
                contract.getPolicyNumber(), "policyNumber(polNo) is required");

        // 메리츠 취소 API — body 조립/파싱은 Client가 처리
        meritzClient.cancelContract(
                company,
                contract.getPolicyNumber(),
                contract.getMeritzQuoteGroupNumber(),
                contract.getMeritzQuoteRequestNumber());

        paymentWriter.markCanceled(payment);

        // 환불 레코드 생성
        refundWriter.create(
                new RefundCommand(
                        contract.getId(),
                        payment.getId(),
                        contract.getTotalPremium(),
                        TravelPaymentMethod.CARD,
                        refundCommand.bankName(),
                        refundCommand.accountNumber(),
                        refundCommand.depositorName(),
                        refundCommand.refundReason()));

        snapshotAppender.append(
                contract.getId(), contract.getInsurerId(), "CANCEL", toJson("CANCELED"));

        return buildResult(contract, payment, plan, insurer);
    }

    private CancelResult buildResult(
            TravelContractEntity contract,
            TravelInsurePaymentEntity payment,
            TravelInsurancePlanEntity plan,
            TravelInsurerEntity insurer) {

        BigDecimal refundAmount = contract.getTotalPremium();

        return new CancelResult(
                new CancelResult.ContractInfo(
                        contract.getId(),
                        contract.getStatus().name(),
                        contract.getPolicyNumber(),
                        contract.getMeritzQuoteGroupNumber(),
                        contract.getMeritzQuoteRequestNumber(),
                        contract.getCountryName(),
                        contract.getCountryCode(),
                        contract.getTotalPremium(),
                        contract.getInsureStartDate(),
                        contract.getInsureEndDate(),
                        new CancelResult.InsurerInfo(
                                insurer.getId(),
                                insurer.getInsurerCode(),
                                insurer.getInsurerName()),
                        new CancelResult.PlanInfo(
                                plan.getId(),
                                plan.getInsuranceProductName(),
                                plan.getPlanName(),
                                plan.getProductCode(),
                                plan.getUnitProductCode(),
                                plan.getPlanGroupCode(),
                                plan.getPlanCode()),
                        new CancelResult.PaymentInfo(
                                payment.getId(),
                                payment.getPaymentMethod().name(),
                                payment.getStatus().name(),
                                payment.getPaidAmount(),
                                payment.getPaymentDate(),
                                payment.getCancelDate(),
                                null),
                        refundAmount));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("[CANCEL] JSON 직렬화 실패", e);
            return "{}";
        }
    }
}

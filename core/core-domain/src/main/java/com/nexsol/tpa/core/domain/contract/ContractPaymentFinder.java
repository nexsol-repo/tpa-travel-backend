package com.nexsol.tpa.core.domain.contract;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.payment.Payment;
import com.nexsol.tpa.core.domain.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContractPaymentFinder {

    private final PaymentRepository paymentRepository;

    public Payment findByContractId(Long contractId) {
        return paymentRepository.findByContractId(contractId).orElse(null);
    }

    public Map<Long, Payment> findMapByContractIds(List<Long> contractIds) {
        return paymentRepository.findByContractIdIn(contractIds).stream()
                .collect(Collectors.toMap(Payment::contractId, p -> p));
    }
}

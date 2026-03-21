package com.nexsol.tpa.core.domain.plan;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.InsurerRepository;
import com.nexsol.tpa.core.error.CoreErrorType;
import com.nexsol.tpa.core.error.CoreException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InsurerReader {

    private final InsurerRepository insurerRepository;

    public Insurer getById(Long insurerId) {
        return insurerRepository
                .findById(insurerId)
                .orElseThrow(
                        () ->
                                new CoreException(
                                        CoreErrorType.NOT_FOUND_DATA,
                                        "insurer not found. insurerId=" + insurerId));
    }
}

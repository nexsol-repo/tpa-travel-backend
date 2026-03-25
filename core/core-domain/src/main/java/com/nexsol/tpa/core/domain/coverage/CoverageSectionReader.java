package com.nexsol.tpa.core.domain.coverage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexsol.tpa.core.domain.repository.CoverageSectionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CoverageSectionReader {

    private final CoverageSectionRepository coverageSectionRepository;

    public List<CoverageSection> readAllActive() {
        return coverageSectionRepository.findAllActive();
    }

    public Map<String, CoverageSection> readAllActiveAsMap() {
        return coverageSectionRepository.findAllActive().stream()
                .collect(Collectors.toMap(CoverageSection::sectionCode, s -> s));
    }
}

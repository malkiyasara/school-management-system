package com.sims.sectionstrategy;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectionStrategyResolver {

    private final List<SectionStrategy> strategies;

    public SectionStrategyResolver(List<SectionStrategy> strategies) {
        this.strategies = strategies;
    }

    public List<String> sectionsFor(int grade) {
        return strategies.stream()
                .filter(s -> s.supports(grade))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No section strategy for grade " + grade))
                .sectionsFor(grade);
    }
}


package com.sims.sectionstrategy;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrimarySections implements SectionStrategy {
    @Override
    public List<String> sectionsFor(int grade) {

        return List.of("A", "B");
    }

    @Override
    public boolean supports(int grade) {

        return grade >= 1 && grade <= 5;
    }
}


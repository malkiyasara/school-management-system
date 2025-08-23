package com.sims.sectionstrategy;

import java.util.List;

public interface SectionStrategy {

    List<String> sectionsFor(int grade);

    boolean supports(int grade);
}

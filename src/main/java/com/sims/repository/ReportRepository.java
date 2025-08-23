package com.sims.repository;

import com.sims.model.Mark;
import org.springframework.data.jpa.repository.JpaRepository;

// Extend JpaRepository for SQL database, or define custom queries here.
public interface ReportRepository extends JpaRepository<Mark, Integer> {
    // Add custom report query methods here as needed.
    // Example:
    // List<Mark> findBySomeReportCriteria(...);
}

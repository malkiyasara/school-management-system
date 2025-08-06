package com.sims.repository;

import com.sims.model.Fee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FeeRepository extends JpaRepository<Fee, Integer> {
    List<Fee> findByStudentId(int studentId);
    List<Fee> findByClassName(String className);
    List<Fee> findByStatus(String status);
    List<Fee> findByDueDateBefore(LocalDate date);
}


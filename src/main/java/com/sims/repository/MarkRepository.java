package com.sims.repository;

import com.sims.model.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MarkRepository extends JpaRepository<Mark, Integer> {
    List<Mark> findByStudentId(int studentId);
    List<Mark> findByExamId(int examId);
}
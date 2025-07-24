package com.sims.repository;

import com.sims.model.ClassTeacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassTeacherRepository extends JpaRepository<ClassTeacher, Integer> {
    Optional<ClassTeacher> findByClassName(String className);
}


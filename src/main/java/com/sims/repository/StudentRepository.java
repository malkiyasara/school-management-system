package com.sims.repository;

import com.sims.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    Student findByEmail(String email);

    List<Student> findByClassName(String className);

    @Query("""
      SELECT s FROM Student s
      WHERE LOWER(CONCAT(COALESCE(s.firstName,''), ' ', COALESCE(s.lastName,'')))
            LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Student> searchByFullName(@Param("name") String name);

    @Query("""
      SELECT s FROM Student s
      WHERE LOWER(s.className) = LOWER(:className)
        AND LOWER(CONCAT(COALESCE(s.firstName,''), ' ', COALESCE(s.lastName,'')))
            LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Student> searchByFullNameAndClass(@Param("name") String name, @Param("className") String className);
}

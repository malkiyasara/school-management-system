package com.sims.repository;

import com.sims.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    
    // Find attendance by student ID
    List<Attendance> findByStudentId(int studentId);
    
    // Find attendance by date
    List<Attendance> findByDate(LocalDate date);
    
    // Find attendance by class name
    List<Attendance> findByClassName(String className);
    
    // Find attendance by student ID and date range
    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByStudentIdAndDateRange(@Param("studentId") int studentId, 
                                                @Param("startDate") LocalDate startDate, 
                                                @Param("endDate") LocalDate endDate);
    
    // Find attendance by class and date
    List<Attendance> findByClassNameAndDate(String className, LocalDate date);
    
    // Find attendance by status
    List<Attendance> findByStatus(String status);
    
    // Find attendance by student ID and status
    List<Attendance> findByStudentIdAndStatus(int studentId, String status);
    
    // Count attendance by status for a student
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.status = :status")
    long countByStudentIdAndStatus(@Param("studentId") int studentId, @Param("status") String status);
    
    // Count total attendance for a student
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId")
    long countByStudentId(@Param("studentId") int studentId);
}

package com.sims.controller;

import com.sims.model.Student;
import com.sims.model.Exam;
import com.sims.model.Mark;
import com.sims.repository.StudentRepository;
import com.sims.repository.ExamRepository;
import com.sims.repository.MarkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ReportController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private MarkRepository markRepository;

    @GetMapping("/reports")
    public String reportsDashboard(Model model) {
        // Get basic statistics
        long totalStudents = studentRepository.count();
        long totalExams = examRepository.count();
        long totalMarks = markRepository.count();
        
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalExams", totalExams);
        model.addAttribute("totalMarks", totalMarks);
        
        return "reports/dashboard";
    }

    @GetMapping("/reports/performance")
    public String performanceReport(Model model) {
        List<Mark> allMarks = markRepository.findAll();
        List<Student> allStudents = studentRepository.findAll();
        List<Exam> allExams = examRepository.findAll();
        
        // Calculate performance metrics
        double averagePercentage = allMarks.stream()
                .mapToDouble(Mark::getPercentage)
                .average()
                .orElse(0.0);
        
        // Calculate pass rate (assuming 50% is pass)
        long passedStudents = allMarks.stream()
                .filter(mark -> mark.getPercentage() >= 50)
                .count();
        double passRate = allMarks.isEmpty() ? 0.0 : (double) passedStudents / allMarks.size() * 100;
        
        // Group marks by class
        Map<String, List<Mark>> marksByClass = allMarks.stream()
                .filter(mark -> mark.getExam() != null)
                .collect(Collectors.groupingBy(mark -> mark.getExam().getClassName()));
        
        // Group marks by subject
        Map<String, List<Mark>> marksBySubject = allMarks.stream()
                .filter(mark -> mark.getExam() != null)
                .collect(Collectors.groupingBy(mark -> mark.getExam().getSubject()));
        
        // Calculate grade distribution
        Map<String, Long> gradeDistribution = allMarks.stream()
                .collect(Collectors.groupingBy(this::getGrade, Collectors.counting()));
        
        // Get top performers (top 10)
        List<Mark> topPerformers = allMarks.stream()
                .filter(mark -> mark.getStudent() != null)
                .sorted((m1, m2) -> Double.compare(m2.getPercentage(), m1.getPercentage()))
                .limit(10)
                .collect(Collectors.toList());
        
        // Get students needing support (bottom 10)
        List<Mark> studentsNeedingSupport = allMarks.stream()
                .filter(mark -> mark.getStudent() != null)
                .sorted((m1, m2) -> Double.compare(m1.getPercentage(), m2.getPercentage()))
                .limit(10)
                .collect(Collectors.toList());
        
        model.addAttribute("allMarks", allMarks);
        model.addAttribute("totalStudents", allStudents.size());
        model.addAttribute("totalExams", allExams.size());
        model.addAttribute("averagePercentage", Math.round(averagePercentage * 100.0) / 100.0);
        model.addAttribute("passRate", Math.round(passRate * 100.0) / 100.0);
        model.addAttribute("marksByClass", marksByClass);
        model.addAttribute("marksBySubject", marksBySubject);
        model.addAttribute("gradeDistribution", gradeDistribution);
        model.addAttribute("topPerformers", topPerformers);
        model.addAttribute("studentsNeedingSupport", studentsNeedingSupport);
        
        return "reports/performance";
    }

    @GetMapping("/reports/class")
    public String classAnalytics(Model model) {
        List<Student> students = studentRepository.findAll();
        List<Exam> exams = examRepository.findAll();
        List<Mark> allMarks = markRepository.findAll();
        
        // Group students by class
        Map<String, List<Student>> studentsByClass = students.stream()
                .collect(Collectors.groupingBy(Student::getClassName));
        
        // Group exams by class
        Map<String, List<Exam>> examsByClass = exams.stream()
                .collect(Collectors.groupingBy(Exam::getClassName));
        
        // Group marks by class and calculate class averages
        Map<String, List<Mark>> marksByClass = allMarks.stream()
                .filter(mark -> mark.getExam() != null)
                .collect(Collectors.groupingBy(mark -> mark.getExam().getClassName()));
        
        // Calculate class averages
        Map<String, Double> classAverages = marksByClass.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().stream()
                            .mapToDouble(Mark::getPercentage)
                            .average()
                            .orElse(0.0)
                ));
        
        // Calculate subject averages across all classes
        Map<String, Double> subjectAverages = allMarks.stream()
                .filter(mark -> mark.getExam() != null)
                .collect(Collectors.groupingBy(
                    mark -> mark.getExam().getSubject(),
                    Collectors.averagingDouble(Mark::getPercentage)
                ));
        
        // Get class performance rankings
        List<Map.Entry<String, Double>> classRankings = classAverages.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        model.addAttribute("studentsByClass", studentsByClass);
        model.addAttribute("examsByClass", examsByClass);
        model.addAttribute("marksByClass", marksByClass);
        model.addAttribute("classAverages", classAverages);
        model.addAttribute("subjectAverages", subjectAverages);
        model.addAttribute("classRankings", classRankings);
        model.addAttribute("totalClasses", studentsByClass.size());
        model.addAttribute("totalStudents", students.size());
        
        return "reports/class";
    }

    @GetMapping("/reports/student/{studentId}")
    public String studentReport(@PathVariable int studentId, Model model) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        List<Mark> studentMarks = markRepository.findByStudentId(studentId);
        
        // Calculate student statistics
        double averagePercentage = studentMarks.stream()
                .mapToDouble(Mark::getPercentage)
                .average()
                .orElse(0.0);
        
        model.addAttribute("student", student);
        model.addAttribute("studentMarks", studentMarks);
        model.addAttribute("averagePercentage", averagePercentage);
        
        return "reports/student";
    }

    @GetMapping("/reports/exam/{examId}")
    public String examReport(@PathVariable int examId, Model model) {
        Exam exam = examRepository.findById(examId).orElseThrow();
        List<Mark> examMarks = markRepository.findByExamId(examId);
        
        // Calculate exam statistics
        double averageMarks = examMarks.stream()
                .mapToDouble(Mark::getObtainedMarks)
                .average()
                .orElse(0.0);
        
        double averagePercentage = examMarks.stream()
                .mapToDouble(Mark::getPercentage)
                .average()
                .orElse(0.0);
        
        model.addAttribute("exam", exam);
        model.addAttribute("examMarks", examMarks);
        model.addAttribute("averageMarks", averageMarks);
        model.addAttribute("averagePercentage", averagePercentage);
        
        return "reports/exam";
    }
    
    // Helper method to determine grade based on percentage
    private String getGrade(Mark mark) {
        double percentage = mark.getPercentage();
        if (percentage >= 90) return "A+";
        else if (percentage >= 80) return "A";
        else if (percentage >= 70) return "B+";
        else if (percentage >= 60) return "B";
        else if (percentage >= 50) return "C";
        else return "F";
    }
}
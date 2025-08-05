package com.sims.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String examName;
    private String subject;
    private String className;
    private LocalDate examDate;
    private int totalMarks;
    private String description;

    public Exam() {}

    public Exam(String examName, String subject, String className, LocalDate examDate, int totalMarks, String description) {
        this.examName = examName;
        this.subject = subject;
        this.className = className;
        this.examDate = examDate;
        this.totalMarks = totalMarks;
        this.description = description;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

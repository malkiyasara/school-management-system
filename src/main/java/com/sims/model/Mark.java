package com.sims.model;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "marks")
public class Mark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    private int obtainedMarks;
    private String remarks;
    private LocalDate recordedDate;

    public Mark() {
        this.recordedDate = LocalDate.now();
    }

    public Mark(Student student, Exam exam, int obtainedMarks, String remarks) {
        this.student = student;
        this.exam = exam;
        this.obtainedMarks = obtainedMarks;
        this.remarks = remarks;
        this.recordedDate = LocalDate.now();
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }

    public int getObtainedMarks() { return obtainedMarks; }
    public void setObtainedMarks(int obtainedMarks) { this.obtainedMarks = obtainedMarks; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDate getRecordedDate() { return recordedDate; }
    public void setRecordedDate(LocalDate recordedDate) { this.recordedDate = recordedDate; }

    // Helper method to calculate percentage
    public double getPercentage() {
        if (exam != null && exam.getTotalMarks() > 0) {
            return (double) obtainedMarks / exam.getTotalMarks() * 100;
        }
        return 0.0;
    }

    @PrePersist
    protected void onCreate() {
        if (recordedDate == null) {
            recordedDate = LocalDate.now();
        }
    }
}

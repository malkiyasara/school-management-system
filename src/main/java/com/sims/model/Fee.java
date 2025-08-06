package com.sims.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "fees")
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private String className;
    private double amount;
    private String status; // PAID, UNPAID, PARTIAL
    private String description;
    private LocalDate dueDate;
    private LocalDate paidDate;

    public Fee() {}

    public Fee(Student student, double amount, String status, LocalDate dueDate, String description) {
        this.student = student;
        this.amount = amount;
        this.status = status;
        this.dueDate = dueDate;
        this.description = description;
        this.className = student != null ? student.getClassName() : null;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }

    @PrePersist
    protected void onCreate() {
        if (status == null || status.isEmpty()) {
            status = "UNPAID";
        }
        if (student != null && className == null) {
            className = student.getClassName();
        }
    }
}


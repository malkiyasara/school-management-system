package com.sims.model;

import jakarta.persistence.*;

import java.util.Arrays;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private int studentID;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(length = 40)
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String className;

    @Transient
    private String password;

    public Student() {
    }

    @Transient
    public String getName() {
        String fn = firstName == null ? "" : firstName.trim();
        String ln = lastName == null ? "" : lastName.trim();
        return (fn + " " + ln).trim();
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            this.firstName = null;
            this.lastName = null;
            return;
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            this.firstName = parts[0];
            this.lastName = "";
        } else {
            this.firstName = parts[0];
            this.lastName = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        }
    }

    public Student(int studentID, String firstName, String lastName, String email,
                   String phoneNumber, String address, String passwordHash, String className) {
        this.studentID = studentID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.passwordHash = passwordHash;
        this.className = (className == null || className.trim().isEmpty()) ? "Not Assigned" : className;
    }

    @Transient
    public String getFullName() {
        String fn = firstName == null ? "" : firstName.trim();
        String ln = lastName == null ? "" : lastName.trim();
        return (fn + " " + ln).trim();
    }

    @PrePersist
    protected void onCreate() {
        if (className == null || className.trim().isEmpty()) {
            className = "Not Assigned";
        }
    }

    // Getters & Setters
    public int getId() { return studentID; }
    public void setId(int id) { this.studentID = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}

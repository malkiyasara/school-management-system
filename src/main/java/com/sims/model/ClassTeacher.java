package com.sims.model;

import jakarta.persistence.*;

@Entity
@Table(name = "class_teachers", uniqueConstraints = @UniqueConstraint(columnNames = {"class_name"}))
public class ClassTeacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "class_name", nullable = false)
    private String className;

    @ManyToOne(optional = false)
    @JoinColumn(name = "teacher_id")
    private AppUser teacher;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public AppUser getTeacher() { return teacher; }
    public void setTeacher(AppUser teacher) { this.teacher = teacher; }
}


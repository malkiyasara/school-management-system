package com.sims.controller;

import com.sims.model.Mark;
import com.sims.model.Student;
import com.sims.repository.MarkRepository;
import com.sims.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MarkRepository markRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/students/select-level")
    public String selectLevel() {
        return "students/level";
    }

    // ===== Step 1: Show all Grades =====
    @GetMapping("/students")
    public String listGrades(@RequestParam(value = "minGrade", required = false) Integer minGrade,
                             @RequestParam(value = "maxGrade", required = false) Integer maxGrade,
                             Model model) {
        // If no filter is provided, send them to the level selection UI
        if (minGrade == null && maxGrade == null) {
            return "redirect:/students/select-level";
        }

        List<Integer> grades = new ArrayList<>();
        for (int i = 1; i <= 13; i++) {
            if ((minGrade == null || i >= minGrade) && (maxGrade == null || i <= maxGrade)) {
                grades.add(i);
            }
        }
        model.addAttribute("grades", grades);
        return "students/grades";
    }


    // ===== Step 2: Show sections inside a grade =====
    @GetMapping("/students/grade/{grade}")
    public String listSections(@PathVariable int grade, Model model) {
        List<String> sections = Arrays.asList("A", "B");
        model.addAttribute("grade", grade);
        model.addAttribute("sections", sections);
        return "students/sections";
    }

    // ===== Step 3: Show students inside a grade + section =====
    @GetMapping("/students/grade/{grade}/{section}")
    public String listStudentsByClass(@PathVariable int grade,
                                      @PathVariable String section,
                                      Model model,
                                      @RequestParam(value = "search", required = false) String search) {
        String className = "Grade " + grade + section.toUpperCase();
        List<Student> students;

        if (search != null && !search.isBlank()) {
            students = studentRepository.searchByFullNameAndClass(search, className);
        } else {
            students = studentRepository.findByClassName(className);
        }

        model.addAttribute("grade", grade);
        model.addAttribute("section", section.toUpperCase());
        model.addAttribute("students", students);
        model.addAttribute("search", search);

        return "students/list";
    }

    // ===== Add Student =====
    @GetMapping("/students/add")
    public String addStudentForm(Model model) {
        model.addAttribute("student", new Student());
        return "students/add";
    }

    @PostMapping("/students/add")
    public String addStudent(@ModelAttribute Student student,
                             BindingResult result,
                             Model model) {
        if (student.getPassword() == null || student.getPassword().isBlank()) {
            result.rejectValue("password", "password.required", "Password is required");
            return "students/add";
        }

        // Check if email already exists
        if (studentRepository.findByEmail(student.getEmail()) != null) {
            model.addAttribute("error", "Email already exists. Please use another one.");
            model.addAttribute("student", student); // keep entered data
            return "students/add";
        }

        if (student.getClassName() == null || student.getClassName().trim().isEmpty()) {
            student.setClassName("Grade 1A");
        }

        student.setPasswordHash(passwordEncoder.encode(student.getPassword()));
        student.setPassword(null);

        studentRepository.save(student);

        return redirectToClass(student.getClassName(), "Student created successfully");
    }

    // ===== Edit Student =====
    @GetMapping("/students/edit/{id}")
    public String editStudentForm(@PathVariable int id, Model model) {
        Student student = studentRepository.findById(id).orElseThrow();
        model.addAttribute("student", student);
        return "students/edit";
    }

    @PostMapping("/students/edit/{id}")
    public String editStudent(@PathVariable int id,
                              @ModelAttribute Student incoming,
                              Model model) {
        Student existing = studentRepository.findById(id).orElseThrow();

        // Check if email belongs to another student
        Student emailOwner = studentRepository.findByEmail(incoming.getEmail());
        if (emailOwner != null && emailOwner.getId() != existing.getId()) {
            model.addAttribute("error", "Email already exists. Please use another one.");
            model.addAttribute("student", existing);
            return "students/edit";
        }

        existing.setFirstName(incoming.getFirstName());
        existing.setLastName(incoming.getLastName());
        existing.setEmail(incoming.getEmail());
        existing.setPhoneNumber(incoming.getPhoneNumber());
        existing.setAddress(incoming.getAddress());

        if (incoming.getClassName() == null || incoming.getClassName().isBlank()) {
            existing.setClassName("Grade 1A");
        } else {
            existing.setClassName(incoming.getClassName());
        }

        if (incoming.getPassword() != null && !incoming.getPassword().isBlank()) {
            existing.setPasswordHash(passwordEncoder.encode(incoming.getPassword()));
        }

        studentRepository.save(existing);

        return redirectToClass(existing.getClassName(), "Student updated successfully");
    }

    // ===== Delete Student =====
    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable int id) {
        try {
            List<Mark> studentMarks = markRepository.findByStudentId(id);
            if (!studentMarks.isEmpty()) {
                markRepository.deleteAll(studentMarks);
            }
            studentRepository.deleteById(id);
            return "redirect:/students/grade/{grade}/{section}?success=Student deleted successfully";
        } catch (Exception e) {
            return "redirect:/students/grade/{grade}/{section}?error=Failed to delete student. They may have associated records.";
        }
    }

    // ===== redirect to correct grade/section page =====
    private String redirectToClass(String className, String message) {
        try {
            String[] parts = className.split(" ");
            if (parts.length == 2 && parts[0].equalsIgnoreCase("Grade")) {
                String gradePart = parts[1];
                int grade = Integer.parseInt(gradePart.substring(0, gradePart.length() - 1));
                String section = gradePart.substring(gradePart.length() - 1);
                return "redirect:/students/grade/" + grade + "/" + section + "?success=" + message;
            }
        } catch (Exception ignored) {
        }
        return "redirect:/students?success=" + message;
    }
}

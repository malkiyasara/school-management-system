package com.sims.controller;

import com.sims.model.Student;
import com.sims.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/student")
public class StudentAuthController {

    @Autowired
    private StudentRepository studentRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Show login page
    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        if (session.getAttribute("studentUser") != null) {
            return "redirect:/student_user/dashboard";
        }
        return "student_auth/login";
    }


    @PostMapping("/login")
    public String loginStudent(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        Student student = studentRepository.findByEmail(email);

        if (student != null && passwordEncoder.matches(password, student.getPasswordHash())) {
            session.setAttribute("studentUser", student);
            return "redirect:/student_user/dashboard";
        } else {
            model.addAttribute("error", "Invalid Email or Password");
            return "student_auth/login";
        }
    }

    // Logout
    @GetMapping("/logout")
    public String logoutStudent(HttpSession session) {
        session.invalidate();
        return "redirect:/student/login";
    }

    // Show register page
    @GetMapping("/register")
    public String showRegisterPage(HttpSession session) {
        if (session.getAttribute("studentUser") != null) {
            return "redirect:/student_user/dashboard";
        }
        return "student_auth/register";
    }

    // Handle register
    @PostMapping("/register")
    public String registerStudent(@RequestParam String firstName,
                                  @RequestParam String lastName,
                                  @RequestParam String email,
                                  @RequestParam String password,
                                  @RequestParam(required = false) String phoneNumber,
                                  @RequestParam(required = false) String address,
                                  @RequestParam(required = false) String className,
                                  Model model) {
        try {
            if (studentRepository.findByEmail(email) != null) {
                throw new IllegalArgumentException("Email already registered!");
            }

            Student newStudent = new Student();
            newStudent.setFirstName(firstName);
            newStudent.setLastName(lastName);
            newStudent.setEmail(email);
            newStudent.setPasswordHash(passwordEncoder.encode(password));
            newStudent.setPhoneNumber(phoneNumber);
            newStudent.setAddress(address);

            // Default class assignment
            if (className == null || className.isBlank()) {
                newStudent.setClassName("Grade 1A");
            } else {
                newStudent.setClassName(className.trim());
            }

            studentRepository.save(newStudent);

            model.addAttribute("success", "Registration Successful! Please login.");
            return "student_auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "student_auth/register";
        }
    }
}

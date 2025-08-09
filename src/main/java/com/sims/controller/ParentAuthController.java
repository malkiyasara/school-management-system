package com.sims.controller;

import com.sims.model.Parent;
import com.sims.service.ParentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
public class ParentAuthController {

    @Autowired
    private ParentService parentService;

    // Show parent login page
    @GetMapping("/parent/login")
    public String showParentLoginPage(HttpSession session) {
        // If parent is already logged in, redirect to portal
        if (session.getAttribute("parent") != null) {
            return "redirect:/portal";
        }
        return "parent/login";
    }

    // Handle parent login
    @PostMapping("/parent/login")
    public String loginParent(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {
        Parent parent = parentService.loginParent(username, password);

        if (parent != null) {
            // Store parent in session
            session.setAttribute("parent", parent);
            session.setAttribute("parentUsername", parent.getUsername());
            session.setAttribute("parentName", parent.getFullName());
            session.setAttribute("studentName", parent.getStudentName());
            session.setAttribute("studentClass", parent.getStudentClass());
            return "redirect:/portal"; // go to portal
        } else {
            model.addAttribute("error", "Invalid Username or Password");
            return "parent/login";
        }
    }

    // Handle parent logout
    @GetMapping("/parent/logout")
    public String logoutParent(HttpSession session) {
        session.invalidate(); // Clear session
        return "redirect:/parent/login";
    }

    // Show parent register page
    @GetMapping("/parent/register")
    public String showParentRegisterPage(HttpSession session) {
        // If parent is already logged in, redirect to portal
        if (session.getAttribute("parent") != null) {
            return "redirect:/portal";
        }
        return "parent/register";
    }

    // Handle parent register
    @PostMapping("/parent/register")
    public String registerParent(@RequestParam String fullName,
                                 @RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam String email,
                                 @RequestParam String phone,
                                 @RequestParam String studentName,
                                 @RequestParam String studentClass,
                                 Model model) {

        // Check if username already exists
        if (parentService.findByUsername(username) != null) {
            model.addAttribute("error", "Username already exists!");
            return "parent/register";
        }

        // Check if email already exists
        if (parentService.findByEmail(email) != null) {
            model.addAttribute("error", "Email already exists!");
            return "parent/register";
        }

        Parent newParent = new Parent();
        newParent.setFullName(fullName);
        newParent.setUsername(username);
        newParent.setPassword(password);
        newParent.setEmail(email);
        newParent.setPhone(phone);
        newParent.setStudentName(studentName);
        newParent.setStudentClass(studentClass);

        parentService.registerParent(newParent);

        model.addAttribute("success", "Registration Successful! Please login.");
        return "parent/login";
    }
}
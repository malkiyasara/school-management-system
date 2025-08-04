package com.sims.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String dashboard(HttpSession session, Model model) {
        // Check if user is logged in
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        // Add user info to model for display
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
        
        return "index";
    }
}
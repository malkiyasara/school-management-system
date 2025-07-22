package com.sims.controller;

import com.sims.model.AppUser;
import com.sims.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // Show login page
    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        // If user is already logged in, redirect to dashboard
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        return "auth/login";
    }

    // Handle login
    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {
        AppUser user = userService.loginUser(username, password);

        if (user != null) {
            // Store user in session
            session.setAttribute("user", user);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("fullName", user.getFullName());
            return "redirect:/"; // go to dashboard
        } else {
            model.addAttribute("error", "Invalid Username or Password");
            return "auth/login";
        }
    }

    // Handle logout
    @GetMapping("/logout")
    public String logoutUser(HttpSession session) {
        session.invalidate(); // Clear session
        return "redirect:/login";
    }

    // Show register page
    @GetMapping("/register")
    public String showRegisterPage(HttpSession session) {
        // If user is already logged in, redirect to dashboard
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        return "auth/register";
    }

    // Handle register
    @PostMapping("/register")
    public String registerUser(@RequestParam String fullName,
                               @RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String role,
                               Model model) {

        // Check if username already exists
        if (userService.findByUsername(username) != null) {
            model.addAttribute("error", "Username already exists!");
            return "auth/register";
        }

        AppUser newUser = new AppUser();
        newUser.setFullName(fullName);
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRole(role);

        userService.registerUser(newUser);

        model.addAttribute("success", "Registration Successful! Please login.");
        return "auth/login";
    }
}

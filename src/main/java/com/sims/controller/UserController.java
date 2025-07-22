package com.sims.controller;

import com.sims.model.AppUser;
import com.sims.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<AppUser> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "users/list";
    }

    @GetMapping("/users/add")
    public String addUserForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "users/add";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute AppUser user) {
        userRepository.save(user);
        return "redirect:/users";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable int id, Model model) {
        AppUser user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        return "users/edit";
    }

    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable int id,
                           @RequestParam String username,
                           @RequestParam String fullName,
                           @RequestParam String role,
                           @RequestParam(defaultValue = "true") boolean active,
                           @RequestParam(required = false) String password) {
        AppUser existing = userRepository.findById(id).orElseThrow();
        existing.setUsername(username);
        existing.setFullName(fullName);
        existing.setRole(role);
        existing.setActive(active);
        if (password != null && !password.isEmpty()) {
            existing.setPassword(password);
        }
        userRepository.save(existing);
        return "redirect:/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable int id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        return "redirect:/users";
    }
}


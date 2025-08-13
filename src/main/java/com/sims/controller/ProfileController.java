package com.sims.controller;

import com.sims.dto.ProfileUpdateRequest;
import com.sims.model.Parent;
import com.sims.service.ParentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/portal/profile")
public class ProfileController {

    @Autowired
    private ParentService parentService;

    @GetMapping("")
    public String showProfilePage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("parentUsername");
        if (username == null) {
            return "redirect:/parent/login";
        }
        
        Parent parent = parentService.findByUsername(username);
        if (parent == null) {
            return "redirect:/parent/login?error=user_not_found";
        }
        
        model.addAttribute("parent", parent);
        
        // Initialize the update request with current user data
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setFullName(parent.getFullName());
        updateRequest.setEmail(parent.getEmail());
        updateRequest.setPhone(parent.getPhone());
        
        model.addAttribute("profileUpdateRequest", updateRequest);
        return "portal/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("profileUpdateRequest") ProfileUpdateRequest updateRequest,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("parentUsername");
        if (username == null) {
            return "redirect:/parent/login";
        }

        try {
            // Get the current parent
            Parent parent = parentService.findByUsername(username);
            if (parent == null) {
                throw new RuntimeException("User not found");
            }
            
            // Validate email format
            if (updateRequest.getEmail() == null || !updateRequest.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new RuntimeException("Please enter a valid email address");
            }
            
            // Validate phone number (basic validation for 10 digits)
            if (updateRequest.getPhone() == null || !updateRequest.getPhone().matches("^\\d{10}$")) {
                throw new RuntimeException("Please enter a valid 10-digit phone number");
            }
            
            // Update basic info
            parent.setFullName(updateRequest.getFullName());
            parent.setEmail(updateRequest.getEmail().trim());
            parent.setPhone(updateRequest.getPhone().trim());
            
            // Update password if provided
            if (updateRequest.getCurrentPassword() != null && !updateRequest.getCurrentPassword().isEmpty()) {
                if (updateRequest.getNewPassword() == null || updateRequest.getNewPassword().isEmpty()) {
                    throw new RuntimeException("New password is required when changing password");
                }
                
                if (!parent.getPassword().equals(updateRequest.getCurrentPassword())) {
                    throw new RuntimeException("Current password is incorrect");
                }
                
                if (!updateRequest.getNewPassword().equals(updateRequest.getConfirmPassword())) {
                    throw new RuntimeException("New password and confirm password do not match");
                }
                
                parent.setPassword(updateRequest.getNewPassword());
            }
            
            // Save the updated parent
            Parent updatedParent = parentService.updateParent(parent);
            
            // Update session attributes
            session.setAttribute("parentName", updatedParent.getFullName());
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
            // Keep the form data on error
            redirectAttributes.addFlashAttribute("profileUpdateRequest", updateRequest);
        }

        return "redirect:/portal/profile";
    }

    @PostMapping("/delete")
    public String deleteProfile(HttpSession session) {
        String username = (String) session.getAttribute("parentUsername");
        if (username != null) {
            try {
                Parent parent = parentService.findByUsername(username);
                if (parent != null) {
                    parentService.deleteParent(parent);
                }
                session.invalidate();
                return "redirect:/parent/login?deleted=true";
            } catch (Exception e) {
                return "redirect:/portal/profile?error=delete_failed";
            }
        }
        return "redirect:/parent/login";
    }
}

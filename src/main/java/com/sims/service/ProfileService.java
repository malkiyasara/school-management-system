package com.sims.service;

import com.sims.dto.ProfileUpdateRequest;
import com.sims.model.Parent;
import com.sims.repository.ParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    @Autowired
    private ParentRepository parentRepository;

    public Parent getParentProfile(String username) {
        return parentRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Parent not found with username: " + username));
    }

    @Transactional
    public Parent updateParentProfile(String username, ProfileUpdateRequest updateRequest) {
        Parent parent = parentRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Parent not found with username: " + username));

        // Update basic info
        parent.setFullName(updateRequest.getFullName());
        parent.setEmail(updateRequest.getEmail());
        parent.setPhone(updateRequest.getPhone());

        // Update password if provided
        if (updateRequest.getCurrentPassword() != null && !updateRequest.getCurrentPassword().isEmpty() &&
            updateRequest.getNewPassword() != null && !updateRequest.getNewPassword().isEmpty()) {
            
            if (!parent.getPassword().equals(updateRequest.getCurrentPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            
            if (!updateRequest.getNewPassword().equals(updateRequest.getConfirmPassword())) {
                throw new RuntimeException("New password and confirm password do not match");
            }
            
            parent.setPassword(updateRequest.getNewPassword());
        }

        return parentRepository.save(parent);
    }

    @Transactional
    public void deleteParentProfile(String username) {
        Parent parent = parentRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Parent not found with username: " + username));
        parentRepository.delete(parent);
    }
}

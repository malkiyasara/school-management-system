package com.sims.service;

import com.sims.model.Parent;
import com.sims.repository.ParentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParentService {

    @Autowired
    private ParentRepository parentRepository;

    // Register parent
    public Parent registerParent(Parent parent) {
        return parentRepository.save(parent);
    }

    // Login parent
    public Parent loginParent(String username, String password) {
        Optional<Parent> parentOpt = parentRepository.findByUsername(username);
        if (parentOpt.isPresent()) {
            Parent parent = parentOpt.get();
            if (parent.getPassword().equals(password)) { // plain password check
                return parent;
            }
        }
        return null;
    }

    // Find parent by username
    public Parent findByUsername(String username) {
        Optional<Parent> parentOpt = parentRepository.findByUsername(username);
        return parentOpt.orElse(null);
    }

    // Find parent by email
    public Parent findByEmail(String email) {
        Optional<Parent> parentOpt = parentRepository.findByEmail(email);
        return parentOpt.orElse(null);
    }

    // Update parent
    @Transactional
    public Parent updateParent(Parent parent) {
        if (!parentRepository.existsById(parent.getId())) {
            throw new RuntimeException("Parent not found with id: " + parent.getId());
        }
        return parentRepository.save(parent);
    }

    // Delete parent
    @Transactional
    public void deleteParent(Parent parent) {
        parentRepository.delete(parent);
    }

    // Get all parents
    public List<Parent> getAllParents() {
        return parentRepository.findAll();
    }
}
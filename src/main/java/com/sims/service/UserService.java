package com.sims.service;

import com.sims.model.AppUser;
import com.sims.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Register user
    public AppUser registerUser(AppUser user) {
        return userRepository.save(user);
    }

    // Login user
    public AppUser loginUser(String username, String password) {
        Optional<AppUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            if (user.getPassword().equals(password)) { // plain password check
                return user;
            }
        }
        return null;
    }

    // Find user by username
    public AppUser findByUsername(String username) {
        Optional<AppUser> userOpt = userRepository.findByUsername(username);
        return userOpt.orElse(null);
    }
}

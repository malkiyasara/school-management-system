package com.sims.repository;

import com.sims.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Integer> {
    Optional<Parent> findByUsername(String username);
    Optional<Parent> findByEmail(String email);
}
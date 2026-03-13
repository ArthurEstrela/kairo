package com.skill.kairo.domain.repository;

import com.skill.kairo.domain.model.user.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    void save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

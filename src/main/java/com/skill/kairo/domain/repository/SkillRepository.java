package com.skill.kairo.domain.repository;

import com.skill.kairo.domain.model.user.Skill;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillRepository {
    List<Skill> findAll();
    Optional<Skill> findById(UUID id);
    void save(Skill skill);
    List<Skill> findByCreatedByUserId(UUID userId, int limit);
    long countByCreatedByUserId(UUID userId);
}

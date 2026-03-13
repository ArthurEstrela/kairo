package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataSkillRepository extends JpaRepository<SkillEntity, UUID> {}

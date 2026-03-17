package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataSkillRepository extends JpaRepository<SkillEntity, UUID> {

    List<SkillEntity> findByCreatedByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByCreatedByUserId(UUID userId);
}

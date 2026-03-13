package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataChallengeRepository extends JpaRepository<ChallengeEntity, UUID> {
    List<ChallengeEntity> findBySkillIdOrderByLevelOrder(UUID skillId);
}

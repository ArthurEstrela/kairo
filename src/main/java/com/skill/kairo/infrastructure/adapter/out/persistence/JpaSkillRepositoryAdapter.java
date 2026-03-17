package com.skill.kairo.infrastructure.adapter.out.persistence;

import com.skill.kairo.domain.model.user.Skill;
import com.skill.kairo.domain.repository.SkillRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SkillEntity;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SpringDataSkillRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaSkillRepositoryAdapter implements SkillRepository {

    private final SpringDataSkillRepository springDataRepository;

    public JpaSkillRepositoryAdapter(SpringDataSkillRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public List<Skill> findAll() {
        return springDataRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Skill> findById(UUID id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void save(Skill skill) {
        springDataRepository.save(toEntity(skill));
    }

    @Override
    public List<Skill> findByCreatedByUserId(UUID userId, int limit) {
        return springDataRepository
                .findByCreatedByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long countByCreatedByUserId(UUID userId) {
        return springDataRepository.countByCreatedByUserId(userId);
    }

    private Skill toDomain(SkillEntity e) {
        // iconUrl is a presentation concern — excluded from domain model, mapped in DTOs
        return new Skill(e.getId(), e.getName(), e.getDescription(),
                e.getDifficultyLevel(), e.getCreatedByUserId(), e.isPublic());
    }

    private SkillEntity toEntity(Skill s) {
        SkillEntity e = new SkillEntity();
        e.setId(s.id());
        e.setName(s.name());
        e.setDescription(s.description());
        e.setDifficultyLevel(s.difficultyLevel());
        e.setCreatedByUserId(s.createdByUserId());
        e.setPublic(s.isPublic());
        e.setCreatedAt(java.time.Instant.now());
        return e;
    }
}

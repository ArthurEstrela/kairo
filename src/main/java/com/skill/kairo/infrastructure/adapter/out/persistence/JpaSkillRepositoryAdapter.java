package com.skill.kairo.infrastructure.adapter.out.persistence;

import com.skill.kairo.domain.model.user.Skill;
import com.skill.kairo.domain.repository.SkillRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SkillEntity;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SpringDataSkillRepository;
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

    private Skill toDomain(SkillEntity entity) {
        return new Skill(entity.getId(), entity.getName(), entity.getDescription(), entity.getDifficultyLevel());
    }
}

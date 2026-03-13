package com.skill.kairo.infrastructure.adapter.out.persistence;

import com.skill.kairo.domain.model.challenge.Challenge;
import com.skill.kairo.domain.repository.ChallengeRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SpringDataChallengeRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.mapper.ChallengeMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaChallengeRepositoryAdapter implements ChallengeRepository {

    private final SpringDataChallengeRepository springDataRepository;
    private final ChallengeMapper mapper;

    public JpaChallengeRepositoryAdapter(
            SpringDataChallengeRepository springDataRepository,
            ChallengeMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Challenge> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Challenge> findBySkillId(UUID skillId) {
        return springDataRepository.findBySkillIdOrderByLevelOrder(skillId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void save(Challenge challenge) {
        springDataRepository.save(mapper.toEntity(challenge));
    }
}

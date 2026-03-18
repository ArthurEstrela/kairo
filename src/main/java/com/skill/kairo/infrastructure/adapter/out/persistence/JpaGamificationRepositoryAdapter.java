package com.skill.kairo.infrastructure.adapter.out.persistence;

import com.skill.kairo.domain.model.gamification.GamificationProfile;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SpringDataGamificationRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.mapper.GamificationMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaGamificationRepositoryAdapter implements GamificationRepository {

    private final SpringDataGamificationRepository springDataRepository;
    private final GamificationMapper mapper;

    public JpaGamificationRepositoryAdapter(SpringDataGamificationRepository springDataRepository, GamificationMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<GamificationProfile> findByUserId(UUID userId) {
        return springDataRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public void save(GamificationProfile profile) {
        springDataRepository.save(mapper.toEntity(profile));
    }

    @Override
    public List<GamificationProfile> findAllWithLivesLostBefore(Instant threshold) {
        return springDataRepository.findAllWithLivesLostBefore(threshold)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public int deductLifeAndReturn(UUID userId) {
        return springDataRepository.deductLifeAndReturn(userId);
    }

    @Override
    public int getLives(UUID userId) {
        return springDataRepository.getLivesByUserId(userId).orElse(0);
    }
}

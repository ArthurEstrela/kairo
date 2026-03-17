package com.skill.kairo.infrastructure.adapter.out.persistence;

import com.skill.kairo.domain.model.challenge.ChallengeProgress;
import com.skill.kairo.domain.repository.ChallengeProgressRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SpringDataChallengeProgressRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaChallengeProgressRepositoryAdapter implements ChallengeProgressRepository {

    private final SpringDataChallengeProgressRepository springDataRepository;

    public JpaChallengeProgressRepositoryAdapter(SpringDataChallengeProgressRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<ChallengeProgress> findByUserIdAndChallengeId(UUID userId, UUID challengeId) {
        return springDataRepository.findByUserIdAndChallengeId(userId, challengeId)
            .map(e -> new ChallengeProgress(e.getId(), e.getUserId(), e.getChallengeId(),
                e.getBestScore(), e.getLastUpdatedAt()));
    }

    @Override
    public List<ChallengeProgress> findByUserIdAndChallengeIdIn(UUID userId, List<UUID> challengeIds) {
        return springDataRepository.findByUserIdAndChallengeIdIn(userId, challengeIds)
            .stream()
            .map(e -> new ChallengeProgress(e.getId(), e.getUserId(), e.getChallengeId(),
                e.getBestScore(), e.getLastUpdatedAt()))
            .toList();
    }

    @Override
    public void upsert(UUID userId, UUID challengeId, int bestScore) {
        springDataRepository.upsertProgress(UUID.randomUUID(), userId, challengeId, bestScore);
    }
}

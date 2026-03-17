package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataChallengeProgressRepository extends JpaRepository<ChallengeProgressEntity, UUID> {

    Optional<ChallengeProgressEntity> findByUserIdAndChallengeId(UUID userId, UUID challengeId);

    List<ChallengeProgressEntity> findByUserIdAndChallengeIdIn(
        UUID userId,
        List<UUID> challengeIds
    );

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO user_challenge_progress (id, user_id, challenge_id, best_score, last_updated_at)
        VALUES (:id, :userId, :challengeId, :bestScore, NOW())
        ON CONFLICT (user_id, challenge_id)
        DO UPDATE SET
            best_score = GREATEST(user_challenge_progress.best_score, EXCLUDED.best_score),
            last_updated_at = CASE
                WHEN EXCLUDED.best_score > user_challenge_progress.best_score THEN NOW()
                ELSE user_challenge_progress.last_updated_at
            END
        """, nativeQuery = true)
    void upsertProgress(
        @Param("id") UUID id,
        @Param("userId") UUID userId,
        @Param("challengeId") UUID challengeId,
        @Param("bestScore") int bestScore
    );
}

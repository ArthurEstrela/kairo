package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_challenge_progress")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChallengeProgressEntity {
    @Id private UUID id;
    private UUID userId;
    private UUID challengeId;
    private int bestScore;
    private Instant lastUpdatedAt;
}

package com.skill.kairo.domain.model.challenge;

import java.time.Instant;
import java.util.UUID;

public class Interaction {

    private final UUID id;
    private final UUID userId;
    private final UUID challengeId;
    private final String userInput;
    private final String aiResponse;
    private final Score score; // Value Object a proteger os limites!
    private final Instant createdAt;

    public Interaction(UUID id, UUID userId, UUID challengeId, String userInput, String aiResponse, Score score) {
        this.id = id;
        this.userId = userId;
        this.challengeId = challengeId;
        this.userInput = userInput;
        this.aiResponse = aiResponse;
        this.score = score;
        this.createdAt = Instant.now();
    }

    // For reconstruction from persistence (preserves the original creation time)
    public Interaction(UUID id, UUID userId, UUID challengeId, String userInput, String aiResponse, Score score, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.challengeId = challengeId;
        this.userInput = userInput;
        this.aiResponse = aiResponse;
        this.score = score;
        this.createdAt = createdAt;
    }

    // --- GETTERS ---
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getChallengeId() { return challengeId; }
    public String getUserInput() { return userInput; }
    public String getAiResponse() { return aiResponse; }
    public Score getScore() { return score; }
    public Instant getCreatedAt() { return createdAt; }
}
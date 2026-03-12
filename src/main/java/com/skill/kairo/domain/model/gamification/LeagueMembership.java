package com.skill.kairo.domain.model.gamification;

import java.util.UUID;

public class LeagueMembership {

    private final UUID userId;
    private final UUID leagueId;
    
    // Mutáveis, mas controlados por comportamentos, não por "setters" burros.
    private int points;
    private int rank;

    public LeagueMembership(UUID userId, UUID leagueId, int points, int rank) {
        if (userId == null || leagueId == null) {
            throw new IllegalArgumentException("UserId e LeagueId são obrigatórios para a Membership.");
        }
        if (points < 0) {
            throw new IllegalArgumentException("Os pontos na liga não podem ser negativos.");
        }
        if (rank <= 0) {
            throw new IllegalArgumentException("O rank inicial deve ser maior que zero.");
        }
        
        this.userId = userId;
        this.leagueId = leagueId;
        this.points = points;
        this.rank = rank;
    }

    // --- COMPORTAMENTOS ---

    public void addPoints(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("A quantidade de pontos a adicionar deve ser positiva.");
        }
        this.points += amount;
    }

    public void updateRank(int newRank) {
        if (newRank <= 0) {
            throw new IllegalArgumentException("O rank deve ser uma posição válida (maior que zero).");
        }
        this.rank = newRank;
    }

    // --- GETTERS ---
    public UUID getUserId() { return userId; }
    public UUID getLeagueId() { return leagueId; }
    public int getPoints() { return points; }
    public int getRank() { return rank; }
}
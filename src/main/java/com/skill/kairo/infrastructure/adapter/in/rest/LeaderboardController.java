package com.skill.kairo.infrastructure.adapter.in.rest;

import com.skill.kairo.infrastructure.adapter.out.cache.RedisLeaderboardAdapter;
import com.skill.kairo.infrastructure.adapter.out.cache.RedisLeaderboardAdapter.LeaderboardEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leaderboard")
public class LeaderboardController {

    private final RedisLeaderboardAdapter leaderboardAdapter;

    public LeaderboardController(RedisLeaderboardAdapter leaderboardAdapter) {
        this.leaderboardAdapter = leaderboardAdapter;
    }

    /**
     * Top N jogadores de uma liga.
     * GET /api/v1/leaderboard/BRONZE?top=10
     */
    @GetMapping("/{tier}")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(
            @PathVariable String tier,
            @RequestParam(defaultValue = "10") int top) {
        List<LeaderboardEntry> entries = leaderboardAdapter.getTopN(tier.toUpperCase(), top);
        return ResponseEntity.ok(entries);
    }

    /**
     * Posição de um utilizador específico na sua liga.
     * GET /api/v1/leaderboard/BRONZE/users/{userId}
     */
    @GetMapping("/{tier}/users/{userId}")
    public ResponseEntity<LeaderboardEntry> getUserRank(
            @PathVariable String tier,
            @PathVariable UUID userId) {
        LeaderboardEntry entry = leaderboardAdapter.getUserRank(tier.toUpperCase(), userId);
        if (entry == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(entry);
    }
}

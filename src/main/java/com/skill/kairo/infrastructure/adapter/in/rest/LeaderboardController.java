package com.skill.kairo.infrastructure.adapter.in.rest;

import com.skill.kairo.domain.model.gamification.LeagueTier;
import com.skill.kairo.infrastructure.adapter.out.cache.RedisLeaderboardAdapter;
import com.skill.kairo.infrastructure.adapter.out.cache.RedisLeaderboardAdapter.LeaderboardEntry;
import com.skill.kairo.infrastructure.security.KairoPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

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
        LeagueTier leagueTier = parseTier(tier);
        List<LeaderboardEntry> entries = leaderboardAdapter.getTopN(leagueTier.name(), top);
        return ResponseEntity.ok(entries);
    }

    /**
     * Posição do utilizador autenticado na sua liga.
     * GET /api/v1/leaderboard/BRONZE/me
     */
    @GetMapping("/{tier}/me")
    public ResponseEntity<LeaderboardEntry> getMyRank(
            @PathVariable String tier,
            @AuthenticationPrincipal KairoPrincipal principal) {
        LeagueTier leagueTier = parseTier(tier);
        LeaderboardEntry entry = leaderboardAdapter.getUserRank(leagueTier.name(), principal.userId());
        if (entry == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(entry);
    }

    private LeagueTier parseTier(String tier) {
        try {
            return LeagueTier.valueOf(tier.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Liga inválida: " + tier + ". Valores aceites: BRONZE, SILVER, GOLD, PLATINUM, DIAMOND");
        }
    }
}

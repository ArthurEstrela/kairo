package com.skill.kairo.infrastructure.adapter.out.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class RedisLeaderboardAdapter {

    // Chave do sorted set: "leaderboard:{tier}" → ex: "leaderboard:BRONZE"
    private static final String KEY_PREFIX = "leaderboard:";

    private final RedisTemplate<String, String> redisTemplate;

    public RedisLeaderboardAdapter(@Qualifier("leaderboardRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Atualiza o XP do utilizador no sorted set da sua liga.
     * Key = "leaderboard:BRONZE", Member = userId (String), Score = xp (double).
     */
    public void updateScore(String tier, UUID userId, double xp) {
        redisTemplate.opsForZSet().add(KEY_PREFIX + tier, userId.toString(), xp);
    }

    /**
     * Devolve o top N jogadores de uma liga, do maior XP para o menor.
     */
    public List<LeaderboardEntry> getTopN(String tier, int n) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(KEY_PREFIX + tier, 0, n - 1);

        List<LeaderboardEntry> entries = new ArrayList<>();
        if (tuples == null) return entries;

        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            entries.add(new LeaderboardEntry(
                    UUID.fromString(tuple.getValue()),
                    rank++,
                    tuple.getScore() != null ? tuple.getScore().intValue() : 0
            ));
        }
        return entries;
    }

    /**
     * Devolve a posição e XP de um utilizador específico na liga.
     */
    public LeaderboardEntry getUserRank(String tier, UUID userId) {
        String key = KEY_PREFIX + tier;
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userId.toString());
        Double score = redisTemplate.opsForZSet().score(key, userId.toString());

        if (rank == null || score == null) return null;
        return new LeaderboardEntry(userId, rank.intValue() + 1, score.intValue());
    }

    public record LeaderboardEntry(UUID userId, int rank, int xp) {}
}

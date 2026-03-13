package com.skill.kairo.infrastructure.adapter.in.event;

import com.skill.kairo.domain.event.LeaguePromotedEvent;
import com.skill.kairo.domain.event.XpAwardedEvent;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.infrastructure.adapter.out.cache.RedisLeaderboardAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BadgeUnlockEventListener {

    private final RedisLeaderboardAdapter leaderboardAdapter;
    private final GamificationRepository gamificationRepository;

    public BadgeUnlockEventListener(
            RedisLeaderboardAdapter leaderboardAdapter,
            GamificationRepository gamificationRepository) {
        this.leaderboardAdapter = leaderboardAdapter;
        this.gamificationRepository = gamificationRepository;
    }

    @EventListener
    public void onXpAwarded(XpAwardedEvent event) {
        // 1. Sincroniza o leaderboard Redis em tempo real
        gamificationRepository.findByUserId(event.userId()).ifPresent(profile ->
                leaderboardAdapter.updateScore(
                        profile.getTier().name(),
                        event.userId(),
                        event.newTotalXp()
                )
        );

        // 2. Desbloquear badges por marcos de XP
        if (event.newTotalXp() >= 1000) {
            System.out.println("[BADGE] Utilizador " + event.userId() + " desbloqueou o Badge de Veterano!");
        }
        if (event.newTotalXp() >= 5000) {
            System.out.println("[BADGE] Utilizador " + event.userId() + " desbloqueou o Badge de Especialista!");
        }
    }

    @EventListener
    public void onLeaguePromoted(LeaguePromotedEvent event) {
        System.out.println("[LIGA] Utilizador " + event.userId() + " promovido para " + event.newTier() + "!");

        // Ao mudar de liga, actualiza o sorted set da nova liga
        gamificationRepository.findByUserId(event.userId()).ifPresent(profile ->
                leaderboardAdapter.updateScore(
                        event.newTier().name(),
                        event.userId(),
                        profile.getCurrentXp()
                )
        );
    }
}

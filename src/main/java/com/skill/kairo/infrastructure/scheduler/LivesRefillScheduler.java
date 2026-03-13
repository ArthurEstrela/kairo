package com.skill.kairo.infrastructure.scheduler;

import com.skill.kairo.domain.model.gamification.GamificationProfile;
import com.skill.kairo.domain.repository.GamificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class LivesRefillScheduler {

    private static final Duration REFILL_INTERVAL = Duration.ofHours(4);

    private final GamificationRepository gamificationRepository;

    public LivesRefillScheduler(GamificationRepository gamificationRepository) {
        this.gamificationRepository = gamificationRepository;
    }

    /**
     * Corre a cada 30 minutos.
     * Para cada perfil com vidas em falta e cujo lastLifeLostAt seja há mais de 4 horas,
     * restaura uma vida.
     */
    @Scheduled(fixedDelay = 30 * 60 * 1000L) // 30 minutos
    public void refillLives() {
        Instant threshold = Instant.now().minus(REFILL_INTERVAL);
        List<GamificationProfile> profiles = gamificationRepository.findAllWithLivesLostBefore(threshold);

        for (GamificationProfile profile : profiles) {
            boolean restored = profile.restoreOneLife();
            if (restored) {
                gamificationRepository.save(profile);
                System.out.println("[SCHEDULER] Vida restaurada para utilizador: " + profile.getUserId()
                        + " | Vidas actuais: " + profile.getCurrentLives() + "/" + profile.getMaxLives());
            }
        }
    }
}

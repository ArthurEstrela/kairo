package com.skill.kairo.infrastructure.scheduler;

import com.skill.kairo.domain.model.gamification.GamificationProfile;
import com.skill.kairo.domain.repository.GamificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class TrackQuotaResetScheduler {

    private static final int MONTHLY_QUOTA = 3;

    private final GamificationRepository gamificationRepository;

    public TrackQuotaResetScheduler(GamificationRepository gamificationRepository) {
        this.gamificationRepository = gamificationRepository;
    }

    /**
     * Corre a meia-noite UTC todos os dias.
     * Para cada perfil Freemium com quota expirada, repõe as 3 gerações mensais.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void resetExpiredQuotas() {
        Instant now = Instant.now();
        List<GamificationProfile> expired = gamificationRepository.findAllWithExpiredQuota(now);
        for (GamificationProfile profile : expired) {
            profile.resetQuota(MONTHLY_QUOTA, now.plus(30, ChronoUnit.DAYS));
            gamificationRepository.save(profile);
        }
    }
}

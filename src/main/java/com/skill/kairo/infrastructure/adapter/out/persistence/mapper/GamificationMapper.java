package com.skill.kairo.infrastructure.adapter.out.persistence.mapper;

import com.skill.kairo.domain.model.gamification.GamificationProfile;
import com.skill.kairo.domain.model.gamification.LeagueTier;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.GamificationProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class GamificationMapper {

    public GamificationProfile toDomain(GamificationProfileEntity entity) {
        return new GamificationProfile(
                entity.getId(),
                entity.getUserId(),
                entity.getCurrentXp(),
                entity.getCurrentLives(),
                entity.getMaxLives(),
                entity.getCurrentStreak(),
                LeagueTier.valueOf(entity.getTier()),
                entity.getLastLifeLostAt(),
                entity.getAvailableTrackGenerations(),
                entity.getQuotaResetDate()
        );
    }

    public GamificationProfileEntity toEntity(GamificationProfile domain) {
        return new GamificationProfileEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getCurrentXp(),
                domain.getCurrentLives(),
                domain.getMaxLives(),
                domain.getCurrentStreak(),
                domain.getTier().name(),
                domain.getLastLifeLostAt(),
                domain.getAvailableTrackGenerations(),
                domain.getQuotaResetDate()
        );
    }
}

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
                null,  // availableTrackGenerations — to be mapped in Task 5
                null   // quotaResetDate — to be mapped in Task 5
        );
    }

    public GamificationProfileEntity toEntity(GamificationProfile domain) {
        // TODO Task 5: add availableTrackGenerations and quotaResetDate when GamificationProfileEntity gets the new fields
        return new GamificationProfileEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getCurrentXp(),
                domain.getCurrentLives(),
                domain.getMaxLives(),
                domain.getCurrentStreak(),
                domain.getTier().name(),
                domain.getLastLifeLostAt()
        );
    }
}

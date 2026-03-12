package com.skill.kairo.domain.repository;

import com.skill.kairo.domain.model.gamification.GamificationProfile;

import java.util.Optional;
import java.util.UUID;

public interface GamificationRepository {
    
    // Procura o perfil de gamificação através do ID do utilizador
    Optional<GamificationProfile> findByUserId(UUID userId);
    
    // Grava (insere ou atualiza) o estado do Agregado
    void save(GamificationProfile profile);
}
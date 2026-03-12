package com.skill.kairo.domain.repository;

import com.skill.kairo.domain.model.challenge.Interaction;

import java.util.List;
import java.util.UUID;

public interface InteractionRepository {
    
    // Regista o histórico da interação com a IA
    void save(Interaction interaction);
    
    // Permite recuperar o histórico de um utilizador (para mostrar no ecrã de perfil)
    List<Interaction> findByUserId(UUID userId);
}
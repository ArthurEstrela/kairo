package com.skill.kairo.domain.repository;

import com.skill.kairo.domain.model.challenge.Challenge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChallengeRepository {
    
    // Recupera um desafio específico para enviar para a IA
    Optional<Challenge> findById(UUID id);
    
    // Recupera todos os desafios de uma habilidade específica (ex: Vendas B2B)
    List<Challenge> findBySkillId(UUID skillId);
    
    // Grava um novo desafio (útil para o teu painel de administrador)
    void save(Challenge challenge);
}
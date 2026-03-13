package com.skill.kairo.domain.repository;

import com.skill.kairo.domain.model.challenge.Interaction;

import java.util.List;
import java.util.UUID;

public interface InteractionRepository {

    void save(Interaction interaction);

    // page é zero-based; size é o número de registos por página
    List<Interaction> findByUserId(UUID userId, int page, int size);
}
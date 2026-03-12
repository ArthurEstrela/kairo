package com.skill.kairo.domain.model.gamification;

import java.util.UUID;

public class Badge {

    private final UUID id;
    private final String name;
    private final String description;

    public Badge(UUID id, String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome da Badge não pode estar vazio.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("A descrição da Badge não pode estar vazia.");
        }
        
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // --- GETTERS ---
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
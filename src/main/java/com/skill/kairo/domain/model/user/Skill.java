package com.skill.kairo.domain.model.user;

import java.util.UUID;

public class Skill {

    private final UUID id;
    private final String name;
    private final String description;

    public Skill(UUID id, String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome da habilidade não pode estar vazio.");
        }
        
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
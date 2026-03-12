package com.skill.kairo.domain.model.user;

import java.time.Instant;
import java.util.UUID;

public class User {

    private final UUID id;
    private final UUID companyId; // Nulo se for B2C (Freemium/Premium)
    private final String email;
    private String passwordHash;
    private String name;
    private final Instant createdAt;

    public User(UUID id, UUID companyId, String email, String passwordHash, String name) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome do usuário é obrigatório.");
        }

        this.id = id;
        this.companyId = companyId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.createdAt = Instant.now();
    }

    // Comportamento para atualizar dados sensíveis
    public void changePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("A nova senha não pode ser vazia.");
        }
        this.passwordHash = newPasswordHash;
    }

    public void updateName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("O nome não pode ser vazio.");
        }
        this.name = newName;
    }

    // --- GETTERS ---
    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
    
    public boolean isCorporateUser() {
        return this.companyId != null;
    }
}
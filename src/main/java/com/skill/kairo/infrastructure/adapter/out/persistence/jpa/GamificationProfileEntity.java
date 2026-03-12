package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "gamification_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GamificationProfileEntity {

    @Id
    private UUID id;
    
    private UUID userId;
    private int currentXp;
    private int currentLives;
    private int maxLives;
    private int currentStreak;
    private String tier; // Guardamos o Enum como String na BD (ex: "BRONZE")

}
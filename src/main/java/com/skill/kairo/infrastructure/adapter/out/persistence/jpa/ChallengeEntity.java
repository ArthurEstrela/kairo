package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "challenges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeEntity {

    @Id
    private UUID id;

    private UUID skillId;
    private String title;
    private int xpReward;
    private int levelOrder;

    private String configType;  // 'ROLEPLAY' ou 'QUIZ'

    @Column(columnDefinition = "TEXT")
    private String configData;  // JSON com os detalhes
}

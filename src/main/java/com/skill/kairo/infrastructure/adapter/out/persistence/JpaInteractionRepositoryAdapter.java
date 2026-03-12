package com.skill.kairo.infrastructure.adapter.out.persistence;

import com.skill.kairo.domain.model.challenge.Interaction;
import com.skill.kairo.domain.model.challenge.Score;
import com.skill.kairo.domain.repository.InteractionRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.InteractionEntity;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SpringDataInteractionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaInteractionRepositoryAdapter implements InteractionRepository {

    private final SpringDataInteractionRepository springDataRepository;

    public JpaInteractionRepositoryAdapter(SpringDataInteractionRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void save(Interaction interaction) {
        // Traduz do Domínio Puro para o JPA Entity
        InteractionEntity entity = new InteractionEntity(
                interaction.getId(),
                interaction.getUserId(),
                interaction.getChallengeId(),
                interaction.getUserInput(),
                interaction.getAiResponse(),
                interaction.getScore().value(), // Extrai o valor do Value Object Score
                interaction.getCreatedAt()
        );
        springDataRepository.save(entity);
    }

    @Override
    public List<Interaction> findByUserId(UUID userId) {
        // Busca do JPA e traduz de volta para o Domínio Puro
        return springDataRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(entity -> new Interaction(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getChallengeId(),
                        entity.getUserInput(),
                        entity.getAiResponse(),
                        new Score(entity.getScore()) // Recria o Value Object Score
                ))
                .collect(Collectors.toList());
    }
}
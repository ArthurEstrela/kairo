package com.skill.kairo.infrastructure.adapter.out.persistence;

import com.skill.kairo.domain.model.challenge.Interaction;
import com.skill.kairo.domain.model.challenge.Score;
import com.skill.kairo.domain.repository.InteractionRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.InteractionEntity;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SpringDataInteractionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaInteractionRepositoryAdapter implements InteractionRepository {

    private final SpringDataInteractionRepository springDataRepository;

    public JpaInteractionRepositoryAdapter(SpringDataInteractionRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void save(Interaction interaction) {
        springDataRepository.save(new InteractionEntity(
                interaction.getId(),
                interaction.getUserId(),
                interaction.getChallengeId(),
                interaction.getUserInput(),
                interaction.getAiResponse(),
                interaction.getScore().value(),
                interaction.getCreatedAt()
        ));
    }

    @Override
    public List<Interaction> findByUserId(UUID userId, int page, int size) {
        return springDataRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .stream()
                .map(entity -> new Interaction(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getChallengeId(),
                        entity.getUserInput(),
                        entity.getAiResponse(),
                        new Score(entity.getScore())
                ))
                .toList();
    }
}
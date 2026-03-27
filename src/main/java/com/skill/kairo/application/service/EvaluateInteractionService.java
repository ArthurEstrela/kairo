package com.skill.kairo.application.service;

import com.skill.kairo.application.command.EvaluateInteractionCommand;
import com.skill.kairo.application.dto.response.InteractionResultResponse;
import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.application.port.EventPublisherPort;
import com.skill.kairo.application.usecase.EvaluateInteractionUseCase;
import com.skill.kairo.domain.event.ChallengeCompletedEvent;
import com.skill.kairo.domain.event.DomainEvent;
import com.skill.kairo.domain.model.challenge.Challenge;
import com.skill.kairo.domain.model.challenge.Interaction;
import com.skill.kairo.domain.model.challenge.InteractionScore;
import com.skill.kairo.domain.model.challenge.Score;
import com.skill.kairo.domain.model.gamification.GamificationProfile;
import com.skill.kairo.domain.repository.ChallengeRepository;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.domain.repository.InteractionRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EvaluateInteractionService implements EvaluateInteractionUseCase {

    private final ChallengeRepository challengeRepository;
    private final GamificationRepository gamificationRepository;
    private final InteractionRepository interactionRepository;
    private final AIPort aiPort;
    private final EventPublisherPort eventPublisher;
    private final TransactionTemplate tx;

    public EvaluateInteractionService(
            ChallengeRepository challengeRepository,
            GamificationRepository gamificationRepository,
            InteractionRepository interactionRepository,
            AIPort aiPort,
            EventPublisherPort eventPublisher,
            PlatformTransactionManager txManager) {
        this.challengeRepository = challengeRepository;
        this.gamificationRepository = gamificationRepository;
        this.interactionRepository = interactionRepository;
        this.aiPort = aiPort;
        this.eventPublisher = eventPublisher;
        this.tx = new TransactionTemplate(txManager);
    }

    @Override
    public InteractionResultResponse execute(EvaluateInteractionCommand command) {

        // 1. Recuperar os Agregados
        Challenge challenge = challengeRepository.findById(command.challengeId())
                .orElseThrow(() -> new IllegalArgumentException("Desafio não encontrado."));

        GamificationProfile profile = gamificationRepository.findByUserId(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("Perfil de gamificação não encontrado."));

        String systemPrompt = challenge.generatePrompt();

        // 2. Se o aiResponse não veio do WebSocket (chamada REST), gerá-lo agora
        String aiResponse = (command.aiResponse() != null && !command.aiResponse().isBlank())
                ? command.aiResponse()
                : aiPort.generateResponse(systemPrompt, command.userInput());

        // 3. Avaliar a qualidade da resposta do utilizador
        InteractionScore interactionScore = aiPort.evaluateInteraction(systemPrompt,
                List.of("", command.userInput())); // index 0 = AI (empty opening), index 1 = user input
        Score score = new Score(interactionScore.score());

        // 4. Aplicar a Regra de Negócio de Gamificação
        String feedback;
        if (score.value() >= 70) {
            profile.awardXp(challenge.getXpReward());
            profile.maintainStreak();
            feedback = "Desafio superado com sucesso!";
        } else {
            profile.failChallenge();
            feedback = "A pontuação não foi suficiente. Tenta novamente!";
        }

        // 5. Guardar interação + perfil atomicamente
        final String finalAiResponse = aiResponse;
        tx.executeWithoutResult(status -> {
            interactionRepository.save(new Interaction(
                    UUID.randomUUID(),
                    profile.getUserId(),
                    challenge.getId(),
                    command.userInput(),
                    finalAiResponse,
                    score
            ));
            gamificationRepository.save(profile);
        });

        // 6. Publicar todos os Eventos de Domínio (após commit)
        List<DomainEvent> events = new ArrayList<>(profile.pullDomainEvents());
        events.add(new ChallengeCompletedEvent(profile.getUserId(), challenge.getId(), score.value()));
        events.forEach(eventPublisher::publish);

        // 7. Devolver a resposta final
        return new InteractionResultResponse(
                score.value(),
                profile.getCurrentXp(),
                profile.getCurrentLives(),
                feedback
        );
    }
}

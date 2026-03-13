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
import com.skill.kairo.domain.model.challenge.Score;
import com.skill.kairo.domain.model.gamification.GamificationProfile;
import com.skill.kairo.domain.repository.ChallengeRepository;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.domain.repository.InteractionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EvaluateInteractionService implements EvaluateInteractionUseCase {

    private final ChallengeRepository challengeRepository;
    private final GamificationRepository gamificationRepository;
    private final InteractionRepository interactionRepository;
    private final AIPort aiPort;
    private final EventPublisherPort eventPublisher;

    public EvaluateInteractionService(
            ChallengeRepository challengeRepository,
            GamificationRepository gamificationRepository,
            InteractionRepository interactionRepository,
            AIPort aiPort,
            EventPublisherPort eventPublisher) {
        this.challengeRepository = challengeRepository;
        this.gamificationRepository = gamificationRepository;
        this.interactionRepository = interactionRepository;
        this.aiPort = aiPort;
        this.eventPublisher = eventPublisher;
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
        Score score = aiPort.evaluateInteraction(systemPrompt, command.userInput());

        // 4. Registar o histórico da interação com a resposta REAL da IA
        interactionRepository.save(new Interaction(
                UUID.randomUUID(),
                profile.getUserId(),
                challenge.getId(),
                command.userInput(),
                aiResponse,
                score
        ));

        // 5. Aplicar a Regra de Negócio de Gamificação
        String feedback;
        if (score.value() >= 70) {
            profile.awardXp(challenge.getXpReward());
            profile.maintainStreak();
            feedback = "Desafio superado com sucesso!";
        } else {
            profile.failChallenge();
            feedback = "A pontuação não foi suficiente. Tenta novamente!";
        }

        // 6. Guardar o novo estado
        gamificationRepository.save(profile);

        // 7. Publicar todos os Eventos de Domínio
        List<DomainEvent> events = new ArrayList<>(profile.pullDomainEvents());
        events.add(new ChallengeCompletedEvent(profile.getUserId(), challenge.getId(), score.value()));
        events.forEach(eventPublisher::publish);

        // 8. Devolver a resposta final
        return new InteractionResultResponse(
                score.value(),
                profile.getCurrentXp(),
                profile.getCurrentLives(),
                feedback
        );
    }
}
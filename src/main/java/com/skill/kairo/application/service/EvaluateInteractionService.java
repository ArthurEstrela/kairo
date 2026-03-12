package com.skill.kairo.application.service;

import com.skill.kairo.application.dto.request.SubmitInteractionRequest;
import com.skill.kairo.application.dto.response.InteractionResultResponse;
import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.application.port.EventPublisherPort;
import com.skill.kairo.application.usecase.EvaluateInteractionUseCase;
import com.skill.kairo.domain.event.DomainEvent;
import com.skill.kairo.domain.model.challenge.Challenge;
import com.skill.kairo.domain.model.challenge.Interaction;
import com.skill.kairo.domain.model.challenge.Score;
import com.skill.kairo.domain.model.gamification.GamificationProfile;
import com.skill.kairo.domain.repository.ChallengeRepository;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.domain.repository.InteractionRepository;

import java.util.List;
import java.util.UUID;

public class EvaluateInteractionService implements EvaluateInteractionUseCase {

    private final ChallengeRepository challengeRepository;
    private final GamificationRepository gamificationRepository;
    private final InteractionRepository interactionRepository;
    private final AIPort aiPort;
    private final EventPublisherPort eventPublisher;

    // Injeção de dependências via construtor (limpo e sem anotações de framework)
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
    public InteractionResultResponse execute(SubmitInteractionRequest request) {
        
        // 1. Recuperar os Agregados da Base de Dados
        Challenge challenge = challengeRepository.findById(request.challengeId())
                .orElseThrow(() -> new IllegalArgumentException("Desafio não encontrado."));
                
        GamificationProfile profile = gamificationRepository.findByUserId(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Perfil de gamificação não encontrado."));

        // 2. Comunicar com a IA (O Agregado Challenge apenas fornece o prompt)
        String systemPrompt = challenge.generatePrompt();
        Score score = aiPort.evaluateInteraction(systemPrompt, request.userInput());

        // 3. Registar o histórico da interação
        Interaction interaction = new Interaction(
                UUID.randomUUID(), 
                profile.getUserId(), 
                challenge.getId(), 
                request.userInput(), 
                "Resposta avaliada pela IA", // Posteriormente, a IA pode devolver um texto detalhado
                score
        );
        interactionRepository.save(interaction);

        // 4. Aplicar a Regra de Negócio (A Gamificação real)
        String feedback;
        if (score.value() >= 70) {
            // Sucesso! Ganha o XP do desafio e mantém a ofensiva (streak)
            profile.awardXp(challenge.getXpReward());
            profile.maintainStreak();
            feedback = "Desafio superado com sucesso!";
        } else {
            // Falha! Perde uma vida e a ofensiva volta a zero
            profile.failChallenge();
            feedback = "A pontuação não foi suficiente. Tenta novamente!";
        }

        // 5. Guardar o novo estado no Repositório
        gamificationRepository.save(profile);

        // 6. Publicar os Eventos de Domínio (O pulo do gato do DDD!)
        List<DomainEvent> eventsToPublish = profile.pullDomainEvents();
        eventsToPublish.forEach(eventPublisher::publish);

        // 7. Devolver a resposta final para o ecrã do utilizador
        return new InteractionResultResponse(
                score.value(),
                profile.getCurrentXp(),
                profile.getCurrentLives(),
                feedback
        );
    }
}
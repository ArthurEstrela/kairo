package com.skill.kairo.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.port.AIPort;
import com.skill.kairo.application.port.EventPublisherPort;
import com.skill.kairo.application.port.JwtPort;
import com.skill.kairo.application.port.PaymentPort;
import com.skill.kairo.application.service.CompleteChallengeService;
import com.skill.kairo.application.service.CreateCheckoutSessionService;
import com.skill.kairo.application.service.EvaluateInteractionService;
import com.skill.kairo.application.service.GenerateTrackService;
import com.skill.kairo.application.service.GetMyTracksService;
import com.skill.kairo.application.service.GetTrackByIdService;
import com.skill.kairo.application.service.LoginService;
import com.skill.kairo.application.service.PasswordEncoderPort;
import com.skill.kairo.application.service.ProcessSubscriptionService;
import com.skill.kairo.application.service.PublishTrackService;
import com.skill.kairo.application.service.RegisterService;
import com.skill.kairo.application.service.RetrieveSessionService;
import com.skill.kairo.application.usecase.CompleteChallengeUseCase;
import com.skill.kairo.application.usecase.CreateCheckoutSessionUseCase;
import com.skill.kairo.application.usecase.EvaluateInteractionUseCase;
import com.skill.kairo.application.usecase.GenerateTrackUseCase;
import com.skill.kairo.application.usecase.GetMyTracksUseCase;
import com.skill.kairo.application.usecase.GetTrackByIdUseCase;
import com.skill.kairo.application.usecase.LoginUseCase;
import com.skill.kairo.application.usecase.ProcessSubscriptionUseCase;
import com.skill.kairo.application.usecase.PublishTrackUseCase;
import com.skill.kairo.application.usecase.RegisterUseCase;
import com.skill.kairo.application.usecase.RetrieveSessionUseCase;
import com.skill.kairo.domain.repository.ChallengeProgressRepository;
import com.skill.kairo.domain.repository.ChallengeRepository;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.domain.repository.InteractionRepository;
import com.skill.kairo.domain.repository.SkillRepository;
import com.skill.kairo.domain.repository.SubscriptionRepository;
import com.skill.kairo.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BeansConfig {

    @Value("${stripe.price.id.premium}")
    private String stripePremiumPriceId;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Bean
    EvaluateInteractionUseCase evaluateInteractionUseCase(
            ChallengeRepository challengeRepository,
            GamificationRepository gamificationRepository,
            InteractionRepository interactionRepository,
            AIPort aiPort,
            EventPublisherPort eventPublisherPort) {
        return new EvaluateInteractionService(
                challengeRepository, gamificationRepository,
                interactionRepository, aiPort, eventPublisherPort);
    }

    @Bean
    RegisterUseCase registerUseCase(
            UserRepository userRepository,
            GamificationRepository gamificationRepository,
            SubscriptionRepository subscriptionRepository,
            JwtPort jwtPort,
            PasswordEncoderPort passwordEncoderPort) {
        return new RegisterService(
                userRepository, gamificationRepository,
                subscriptionRepository, jwtPort, passwordEncoderPort);
    }

    @Bean
    LoginUseCase loginUseCase(
            UserRepository userRepository,
            JwtPort jwtPort,
            PasswordEncoderPort passwordEncoderPort) {
        return new LoginService(userRepository, jwtPort, passwordEncoderPort);
    }

    @Bean
    ProcessSubscriptionUseCase processSubscriptionUseCase(
            PaymentPort paymentPort,
            SubscriptionRepository subscriptionRepository,
            GamificationRepository gamificationRepository) {
        return new ProcessSubscriptionService(
                paymentPort, subscriptionRepository, gamificationRepository);
    }

    @Bean
    GenerateTrackUseCase generateTrackUseCase(
            AIPort aiPort,
            SkillRepository skillRepository,
            ChallengeRepository challengeRepository,
            ObjectMapper objectMapper,
            PlatformTransactionManager txManager,
            GamificationRepository gamificationRepository) {
        return new GenerateTrackService(aiPort, skillRepository, challengeRepository,
                                        objectMapper, txManager, gamificationRepository);
    }

    @Bean
    GetMyTracksUseCase getMyTracksUseCase(
            SkillRepository skillRepository,
            ChallengeRepository challengeRepository,
            ChallengeProgressRepository progressRepository) {
        return new GetMyTracksService(skillRepository, challengeRepository, progressRepository);
    }

    @Bean
    GetTrackByIdUseCase getTrackByIdUseCase(
            SkillRepository skillRepository,
            ChallengeRepository challengeRepository,
            ChallengeProgressRepository progressRepository) {
        return new GetTrackByIdService(skillRepository, challengeRepository, progressRepository);
    }

    @Bean
    PublishTrackUseCase publishTrackUseCase(SkillRepository skillRepository) {
        return new PublishTrackService(skillRepository);
    }

    @Bean
    CompleteChallengeUseCase completeChallengeUseCase(
            InteractionRepository interactionRepository,
            ChallengeProgressRepository progressRepository,
            PlatformTransactionManager txManager) {
        return new CompleteChallengeService(interactionRepository, progressRepository, txManager);
    }

    @Bean
    CreateCheckoutSessionUseCase createCheckoutSessionUseCase(PaymentPort paymentPort) {
        return new CreateCheckoutSessionService(
            paymentPort,
            stripePremiumPriceId,
            frontendUrl + "/dashboard/upgrade/return"
        );
    }

    @Bean
    RetrieveSessionUseCase retrieveSessionUseCase(PaymentPort paymentPort) {
        return new RetrieveSessionService(paymentPort);
    }
}

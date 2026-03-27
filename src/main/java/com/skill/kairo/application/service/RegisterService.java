package com.skill.kairo.application.service;

import com.skill.kairo.application.dto.request.RegisterRequest;
import com.skill.kairo.application.dto.response.AuthResponse;
import com.skill.kairo.application.port.JwtPort;
import com.skill.kairo.application.usecase.RegisterUseCase;
import com.skill.kairo.domain.exception.EmailAlreadyExistsException;
import com.skill.kairo.domain.model.gamification.GamificationProfile;
import com.skill.kairo.domain.model.subscription.Subscription;
import com.skill.kairo.domain.model.user.User;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.domain.repository.SubscriptionRepository;
import com.skill.kairo.domain.repository.UserRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

public class RegisterService implements RegisterUseCase {

    private final UserRepository userRepository;
    private final GamificationRepository gamificationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final JwtPort jwtPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TransactionTemplate tx;

    public RegisterService(
            UserRepository userRepository,
            GamificationRepository gamificationRepository,
            SubscriptionRepository subscriptionRepository,
            JwtPort jwtPort,
            PasswordEncoderPort passwordEncoderPort,
            PlatformTransactionManager txManager) {
        this.userRepository = userRepository;
        this.gamificationRepository = gamificationRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.jwtPort = jwtPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tx = new TransactionTemplate(txManager);
    }

    @Override
    public AuthResponse execute(RegisterRequest request) {
        // 1. Garante unicidade do email (outside transaction — fast read)
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        UUID userId = UUID.randomUUID();
        String hashedPassword = passwordEncoderPort.encode(request.password());
        User newUser = new User(userId, null, request.email(), hashedPassword, request.name());

        // 2. Persist user, gamification profile and subscription atomically
        tx.executeWithoutResult(status -> {
            userRepository.save(newUser);

            GamificationProfile profile = new GamificationProfile(UUID.randomUUID(), userId);
            gamificationRepository.save(profile);

            Subscription subscription = Subscription.createDefaultFreemium(userId);
            subscriptionRepository.save(subscription);
        });

        // 3. Gera e devolve o JWT (after commit)
        String token = jwtPort.generateToken(userId, newUser.getEmail());
        return new AuthResponse(token, userId, newUser.getName(), newUser.getEmail());
    }
}

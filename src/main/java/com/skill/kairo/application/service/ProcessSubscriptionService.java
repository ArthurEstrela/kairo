package com.skill.kairo.application.service;

import com.skill.kairo.application.dto.StripeSubscriptionEvent;
import com.skill.kairo.application.port.PaymentPort;
import com.skill.kairo.application.usecase.ProcessSubscriptionUseCase;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.domain.repository.SubscriptionRepository;

import java.util.UUID;

public class ProcessSubscriptionService implements ProcessSubscriptionUseCase {

    private final PaymentPort paymentPort;
    private final SubscriptionRepository subscriptionRepository;
    private final GamificationRepository gamificationRepository;

    public ProcessSubscriptionService(
            PaymentPort paymentPort,
            SubscriptionRepository subscriptionRepository,
            GamificationRepository gamificationRepository) {
        this.paymentPort = paymentPort;
        this.subscriptionRepository = subscriptionRepository;
        this.gamificationRepository = gamificationRepository;
    }

    @Override
    public void execute(String stripePayload, String stripeSignatureHeader) {
        // Verificação de assinatura + parsing JSON delegados ao adapter (infra)
        StripeSubscriptionEvent event = paymentPort.verifyAndParseWebhook(stripePayload, stripeSignatureHeader);

        switch (event.eventType()) {
            case "customer.subscription.created", "customer.subscription.updated" -> {
                var subscription = subscriptionRepository
                        .findByStripeCustomerId(event.stripeCustomerId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Subscrição não encontrada para customer: " + event.stripeCustomerId()));

                subscription.upgradeToPremium(
                        event.stripeCustomerId(),
                        event.stripeSubscriptionId(),
                        event.currentPeriodEnd());
                subscriptionRepository.save(subscription);

                // Restaurar vidas ao fazer upgrade para Premium
                gamificationRepository.findByUserId(subscription.getUserId()).ifPresent(profile -> {
                    profile.enableUnlimitedGenerations();
                    profile.restoreLives();
                    gamificationRepository.save(profile);
                });
            }
            case "customer.subscription.deleted" ->
                    subscriptionRepository.findByStripeCustomerId(event.stripeCustomerId())
                            .ifPresent(sub -> {
                                sub.cancel();
                                subscriptionRepository.save(sub);
                            });

            case "invoice.payment_succeeded" -> {
                if (event.currentPeriodEnd() != null) {
                    subscriptionRepository.findByStripeCustomerId(event.stripeCustomerId())
                            .ifPresent(sub -> {
                                sub.renew(event.currentPeriodEnd());
                                subscriptionRepository.save(sub);
                            });
                }
            }
            case "checkout.session.completed" -> {
                if (event.kairoUserId() == null) return;  // evento sem metadata → ignorar
                UUID userId = UUID.fromString(event.kairoUserId());
                var subscription = subscriptionRepository.findByUserId(userId)
                        .orElseThrow(() -> new IllegalStateException("Subscrição não encontrada"));
                // Só associar IDs Stripe se ambos estiverem presentes — caso contrário
                // customer.subscription.created trata do link (chega instantes depois)
                if (event.stripeCustomerId() != null && event.stripeSubscriptionId() != null) {
                    subscription.upgradeToPremium(
                            event.stripeCustomerId(),
                            event.stripeSubscriptionId(),
                            null  // currentPeriodEnd chega via invoice.payment_succeeded
                    );
                    subscriptionRepository.save(subscription);
                }
                gamificationRepository.findByUserId(userId).ifPresent(profile -> {
                    profile.enableUnlimitedGenerations();
                    profile.restoreLives();
                    gamificationRepository.save(profile);
                });
            }
            default -> {} // Eventos não tratados são ignorados
        }
    }
}

package com.skill.kairo.application.service;

import com.skill.kairo.application.dto.StripeSubscriptionEvent;
import com.skill.kairo.application.port.PaymentPort;
import com.skill.kairo.application.usecase.ProcessSubscriptionUseCase;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.domain.repository.SubscriptionRepository;

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
            default -> {} // Eventos não tratados são ignorados
        }
    }
}

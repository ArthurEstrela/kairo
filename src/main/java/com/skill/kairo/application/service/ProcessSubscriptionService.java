package com.skill.kairo.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.port.PaymentPort;
import com.skill.kairo.application.usecase.ProcessSubscriptionUseCase;
import com.skill.kairo.domain.model.gamification.GamificationProfile;
import com.skill.kairo.domain.model.subscription.Subscription;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.domain.repository.SubscriptionRepository;

import java.time.Instant;

public class ProcessSubscriptionService implements ProcessSubscriptionUseCase {

    private final PaymentPort paymentPort;
    private final SubscriptionRepository subscriptionRepository;
    private final GamificationRepository gamificationRepository;
    private final ObjectMapper objectMapper;

    public ProcessSubscriptionService(
            PaymentPort paymentPort,
            SubscriptionRepository subscriptionRepository,
            GamificationRepository gamificationRepository,
            ObjectMapper objectMapper) {
        this.paymentPort = paymentPort;
        this.subscriptionRepository = subscriptionRepository;
        this.gamificationRepository = gamificationRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(String stripePayload, String stripeSignatureHeader) {
        try {
            // 1. Verifica a assinatura do Stripe (segurança crítica)
            String verifiedPayload = paymentPort.verifyAndParseWebhook(stripePayload, stripeSignatureHeader);
            JsonNode event = objectMapper.readTree(verifiedPayload);

            String eventType = event.get("type").asText();
            JsonNode dataObject = event.path("data").path("object");

            switch (eventType) {
                case "customer.subscription.created", "customer.subscription.updated" -> {
                    String stripeCustomerId = dataObject.get("customer").asText();
                    String stripeSubscriptionId = dataObject.get("id").asText();
                    long periodEndEpoch = dataObject.get("current_period_end").asLong();
                    Instant periodEnd = Instant.ofEpochSecond(periodEndEpoch);

                    Subscription subscription = subscriptionRepository
                            .findByStripeCustomerId(stripeCustomerId)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Subscrição não encontrada para customer: " + stripeCustomerId));

                    subscription.upgradeToPremium(stripeCustomerId, stripeSubscriptionId, periodEnd);
                    subscriptionRepository.save(subscription);

                    // Restaurar vidas completas ao fazer upgrade para Premium
                    gamificationRepository.findByUserId(subscription.getUserId()).ifPresent(profile -> {
                        profile.restoreLives();
                        gamificationRepository.save(profile);
                    });
                }
                case "customer.subscription.deleted" -> {
                    String stripeCustomerId = dataObject.get("customer").asText();

                    subscriptionRepository.findByStripeCustomerId(stripeCustomerId)
                            .ifPresent(subscription -> {
                                subscription.cancel();
                                subscriptionRepository.save(subscription);
                            });
                }
                case "invoice.payment_succeeded" -> {
                    String stripeCustomerId = dataObject.get("customer").asText();
                    long periodEndEpoch = dataObject.path("lines").path("data")
                            .path(0).path("period").path("end").asLong();

                    if (periodEndEpoch > 0) {
                        subscriptionRepository.findByStripeCustomerId(stripeCustomerId)
                                .ifPresent(subscription -> {
                                    subscription.renew(Instant.ofEpochSecond(periodEndEpoch));
                                    subscriptionRepository.save(subscription);
                                });
                    }
                }
                default -> {
                    // Eventos não tratados são ignorados silenciosamente
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar webhook do Stripe: " + e.getMessage(), e);
        }
    }
}

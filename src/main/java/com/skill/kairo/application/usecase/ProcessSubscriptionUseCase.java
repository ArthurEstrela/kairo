package com.skill.kairo.application.usecase;

public interface ProcessSubscriptionUseCase {
    /**
     * Processa um evento de webhook do Stripe.
     * Atualiza o estado da subscrição do utilizador (upgrade, cancelamento, renovação).
     */
    void execute(String stripePayload, String stripeSignatureHeader);
}

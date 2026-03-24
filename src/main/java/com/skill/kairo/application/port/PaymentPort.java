package com.skill.kairo.application.port;

import com.skill.kairo.application.dto.StripeSubscriptionEvent;
import com.skill.kairo.application.dto.response.CheckoutSessionResponse;
import com.skill.kairo.application.dto.response.SessionStatusResponse;

import java.util.UUID;

public interface PaymentPort {
    /**
     * Verifica a assinatura do webhook Stripe, parseia o payload e devolve um DTO.
     * Toda dependência de Stripe SDK / Jackson fica confinada ao adapter (infra).
     * Lança IllegalArgumentException se a assinatura for inválida.
     */
    StripeSubscriptionEvent verifyAndParseWebhook(String payload, String stripeSignatureHeader);

    CheckoutSessionResponse createEmbeddedSession(UUID userId, String userEmail, String priceId, String returnUrl);

    SessionStatusResponse retrieveSession(String sessionId);
}

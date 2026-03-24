package com.skill.kairo.infrastructure.adapter.out.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.dto.StripeSubscriptionEvent;
import com.skill.kairo.application.dto.response.CheckoutSessionResponse;
import com.skill.kairo.application.dto.response.SessionStatusResponse;
import com.skill.kairo.application.port.PaymentPort;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class StripeAdapter implements PaymentPort {

    private final String webhookSecret;
    private final ObjectMapper objectMapper;

    public StripeAdapter(
            @Value("${stripe.webhook.secret}") String webhookSecret,
            @Value("${stripe.api.key}") String apiKey,
            ObjectMapper objectMapper) {
        this.webhookSecret = webhookSecret;
        this.objectMapper = objectMapper;
        Stripe.apiKey = apiKey;  // inicialização estática do SDK — obrigatório antes de qualquer chamada
    }

    @Override
    public StripeSubscriptionEvent verifyAndParseWebhook(String payload, String stripeSignatureHeader) {
        try {
            Event event = Webhook.constructEvent(payload, stripeSignatureHeader, webhookSecret);
            String eventType = event.getType();
            JsonNode dataObject = objectMapper.readTree(payload).path("data").path("object");

            // checkout.session.completed tem estrutura JSON diferente dos outros eventos
            if ("checkout.session.completed".equals(eventType)) {
                String customerId     = dataObject.path("customer").asText(null);
                String subscriptionId = dataObject.path("subscription").asText(null);
                String userId         = dataObject.path("metadata").path("kairo_user_id").asText(null);
                return new StripeSubscriptionEvent(eventType, customerId, subscriptionId, null, userId);
            }

            // Eventos restantes: extrair campos standard
            String stripeCustomerId     = dataObject.path("customer").asText(null);
            String stripeSubscriptionId = dataObject.path("id").asText(null);

            long periodEndEpoch = switch (eventType) {
                case "invoice.payment_succeeded" ->
                    dataObject.path("lines").path("data").path(0).path("period").path("end").asLong(0);
                default ->
                    dataObject.path("current_period_end").asLong(0);
            };
            Instant currentPeriodEnd = periodEndEpoch > 0 ? Instant.ofEpochSecond(periodEndEpoch) : null;

            return new StripeSubscriptionEvent(eventType, stripeCustomerId, stripeSubscriptionId,
                                               currentPeriodEnd, null);

        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Assinatura do webhook Stripe inválida.", e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar payload do Stripe", e);
        }
    }

    @Override
    public CheckoutSessionResponse createEmbeddedSession(UUID userId, String userEmail,
                                                          String priceId, String returnUrl) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                .setReturnUrl(returnUrl)
                .setCustomerEmail(userEmail)
                .putMetadata("kairo_user_id", userId.toString())
                .addLineItem(SessionCreateParams.LineItem.builder()
                    .setPrice(priceId)
                    .setQuantity(1L)
                    .build())
                .build();

            Session session = Session.create(params);
            return new CheckoutSessionResponse(session.getClientSecret());

        } catch (StripeException e) {
            throw new RuntimeException("Erro ao criar sessão de checkout Stripe", e);
        }
    }

    @Override
    public SessionStatusResponse retrieveSession(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            return new SessionStatusResponse(session.getStatus());
        } catch (StripeException e) {
            throw new RuntimeException("Erro ao verificar sessão Stripe", e);
        }
    }
}

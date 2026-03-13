package com.skill.kairo.infrastructure.adapter.out.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skill.kairo.application.dto.StripeSubscriptionEvent;
import com.skill.kairo.application.port.PaymentPort;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Adapter Stripe: verifica a assinatura e converte o payload JSON para StripeSubscriptionEvent.
 * Jackson e Stripe SDK ficam confinados aqui — a camada de aplicação não os conhece.
 */
@Component
public class StripeAdapter implements PaymentPort {

    private final String webhookSecret;
    private final ObjectMapper objectMapper;

    public StripeAdapter(
            @Value("${stripe.webhook.secret}") String webhookSecret,
            ObjectMapper objectMapper) {
        this.webhookSecret = webhookSecret;
        this.objectMapper = objectMapper;
    }

    @Override
    public StripeSubscriptionEvent verifyAndParseWebhook(String payload, String stripeSignatureHeader) {
        try {
            // 1. Verificar assinatura (segurança crítica — rejeita replays e payloads forjados)
            Event event = Webhook.constructEvent(payload, stripeSignatureHeader, webhookSecret);
            String eventType = event.getType();

            // 2. Parsear o payload já verificado
            JsonNode dataObject = objectMapper.readTree(payload).path("data").path("object");
            String stripeCustomerId = dataObject.path("customer").asText(null);
            String stripeSubscriptionId = dataObject.path("id").asText(null);

            // 3. Extrair currentPeriodEnd conforme o tipo de evento
            long periodEndEpoch = switch (eventType) {
                case "invoice.payment_succeeded" ->
                        dataObject.path("lines").path("data").path(0).path("period").path("end").asLong(0);
                default ->
                        dataObject.path("current_period_end").asLong(0);
            };

            Instant currentPeriodEnd = periodEndEpoch > 0 ? Instant.ofEpochSecond(periodEndEpoch) : null;

            return new StripeSubscriptionEvent(eventType, stripeCustomerId, stripeSubscriptionId, currentPeriodEnd);

        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Assinatura do webhook Stripe inválida.", e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar payload do Stripe", e);
        }
    }
}

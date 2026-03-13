package com.skill.kairo.infrastructure.adapter.out.payment;

import com.skill.kairo.application.port.PaymentPort;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StripeAdapter implements PaymentPort {

    private final String webhookSecret;

    public StripeAdapter(@Value("${stripe.webhook.secret}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    @Override
    public String verifyAndParseWebhook(String payload, String stripeSignatureHeader) {
        try {
            Event event = Webhook.constructEvent(payload, stripeSignatureHeader, webhookSecret);
            return event.toJson();
        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Assinatura do webhook Stripe inválida.", e);
        }
    }
}

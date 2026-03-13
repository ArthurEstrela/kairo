package com.skill.kairo.application.port;

public interface PaymentPort {
    /**
     * Verifica a assinatura do webhook Stripe e devolve o JSON do evento.
     * Lança exceção se a assinatura for inválida.
     */
    String verifyAndParseWebhook(String payload, String stripeSignatureHeader);
}

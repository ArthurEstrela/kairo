package com.skill.kairo.infrastructure.adapter.in.rest;

import com.skill.kairo.application.usecase.ProcessSubscriptionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookStripeController {

    private final ProcessSubscriptionUseCase processSubscriptionUseCase;

    public WebhookStripeController(ProcessSubscriptionUseCase processSubscriptionUseCase) {
        this.processSubscriptionUseCase = processSubscriptionUseCase;
    }

    /**
     * Endpoint que o Stripe chama quando ocorre um evento de pagamento.
     * Nota: Esta rota deve ser excluída do JWT filter (configurado no SecurityConfig).
     */
    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String stripeSignature) {

        processSubscriptionUseCase.execute(payload, stripeSignature);
        return ResponseEntity.ok().build();
    }
}

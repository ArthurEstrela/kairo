package com.skill.kairo.infrastructure.adapter.in.rest;

import com.skill.kairo.application.dto.response.CheckoutSessionResponse;
import com.skill.kairo.application.dto.response.SessionStatusResponse;
import com.skill.kairo.application.usecase.CreateCheckoutSessionUseCase;
import com.skill.kairo.application.usecase.RetrieveSessionUseCase;
import com.skill.kairo.infrastructure.security.KairoPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final CreateCheckoutSessionUseCase createCheckoutSession;
    private final RetrieveSessionUseCase retrieveSession;

    public SubscriptionController(CreateCheckoutSessionUseCase createCheckoutSession,
                                  RetrieveSessionUseCase retrieveSession) {
        this.createCheckoutSession = createCheckoutSession;
        this.retrieveSession = retrieveSession;
    }

    /**
     * Cria uma sessão de checkout Stripe Embedded.
     * Requer JWT válido — o userId e email são extraídos do token.
     */
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutSessionResponse> createCheckout(
            @AuthenticationPrincipal KairoPrincipal principal) {
        CheckoutSessionResponse response = createCheckoutSession.execute(
            principal.userId(), principal.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Devolve o status da sessão Stripe (complete | open | expired).
     * Público — chamado pelo frontend na return URL após pagamento.
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<SessionStatusResponse> getSessionStatus(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(retrieveSession.execute(sessionId));
    }
}

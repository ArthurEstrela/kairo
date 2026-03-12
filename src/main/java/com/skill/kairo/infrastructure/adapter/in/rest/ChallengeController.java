package com.skill.kairo.infrastructure.adapter.in.rest;

import com.skill.kairo.application.dto.request.SubmitInteractionRequest;
import com.skill.kairo.application.dto.response.InteractionResultResponse;
import com.skill.kairo.application.usecase.EvaluateInteractionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeController {

    // Dependemos da PORTA de entrada (Interface), não da implementação concreta!
    private final EvaluateInteractionUseCase evaluateInteractionUseCase;

    public ChallengeController(EvaluateInteractionUseCase evaluateInteractionUseCase) {
        this.evaluateInteractionUseCase = evaluateInteractionUseCase;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<InteractionResultResponse> evaluateInteraction(@RequestBody SubmitInteractionRequest request) {
        // 1. Recebe o JSON do Next.js
        // 2. Entrega ao Maestro (O nosso Application Service purista)
        InteractionResultResponse response = evaluateInteractionUseCase.execute(request);
        
        // 3. Devolve a resposta processada (XP, Vidas, Feedback) para o frontend animar a interface!
        return ResponseEntity.ok(response);
    }
}
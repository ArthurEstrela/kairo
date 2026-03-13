package com.skill.kairo.infrastructure.adapter.in.rest;

import com.skill.kairo.application.command.EvaluateInteractionCommand;
import com.skill.kairo.application.dto.request.SubmitInteractionRequest;
import com.skill.kairo.application.dto.response.InteractionResultResponse;
import com.skill.kairo.application.usecase.EvaluateInteractionUseCase;
import com.skill.kairo.infrastructure.security.KairoPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeController {

    private final EvaluateInteractionUseCase evaluateInteractionUseCase;

    public ChallengeController(EvaluateInteractionUseCase evaluateInteractionUseCase) {
        this.evaluateInteractionUseCase = evaluateInteractionUseCase;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<InteractionResultResponse> evaluateInteraction(
            @Valid @RequestBody SubmitInteractionRequest request) {

        // userId vem do JWT (nunca do cliente) — evita IDOR
        KairoPrincipal principal = (KairoPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        // aiResponse=null → o service chama a IA internamente (caminho REST, sem streaming)
        EvaluateInteractionCommand command = new EvaluateInteractionCommand(
                principal.userId(), request.challengeId(), request.userInput(), null
        );

        return ResponseEntity.ok(evaluateInteractionUseCase.execute(command));
    }
}
package com.skill.kairo.infrastructure.adapter.in.rest;

import com.skill.kairo.application.dto.request.CompleteChallengeRequest;
import com.skill.kairo.domain.exception.TrackGenerationLimitException;
import com.skill.kairo.application.dto.request.GenerateTrackRequest;
import com.skill.kairo.application.dto.response.GenerateTrackResponse;
import com.skill.kairo.application.dto.response.TrackWithChallengesResponse;
import com.skill.kairo.application.usecase.*;
import com.skill.kairo.infrastructure.security.KairoPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TrackController {

    private final GenerateTrackUseCase generateTrack;
    private final GetMyTracksUseCase getMyTracks;
    private final GetTrackByIdUseCase getTrackById;
    private final PublishTrackUseCase publishTrack;
    private final CompleteChallengeUseCase completeChallenge;

    public TrackController(GenerateTrackUseCase generateTrack,
                           GetMyTracksUseCase getMyTracks,
                           GetTrackByIdUseCase getTrackById,
                           PublishTrackUseCase publishTrack,
                           CompleteChallengeUseCase completeChallenge) {
        this.generateTrack = generateTrack;
        this.getMyTracks = getMyTracks;
        this.getTrackById = getTrackById;
        this.publishTrack = publishTrack;
        this.completeChallenge = completeChallenge;
    }

    @PostMapping("/tracks/generate")
    public ResponseEntity<?> generate(@RequestBody GenerateTrackRequest req,
                                      @AuthenticationPrincipal KairoPrincipal principal) {
        if (req.goal() == null || req.goal().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "O objetivo não pode estar vazio."));
        }
        if (req.goal().length() > 500) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "A descrição é demasiado longa (máx. 500 caracteres)."));
        }
        try {
            GenerateTrackResponse response = generateTrack.execute(principal.userId(), req.goal());
            return ResponseEntity.ok(response);
        } catch (TrackGenerationLimitException e) {
            throw e;  // propagate to GlobalExceptionHandler → 429
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if ("AI_ERROR".equals(msg) || "AI_PARSE_ERROR".equals(msg)) {
                return ResponseEntity.status(422)
                    .body(Map.of("error", "Não foi possível gerar a trilha. Tenta de novo."));
            }
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno. Tenta de novo."));
        }
    }

    @GetMapping("/tracks/my")
    public ResponseEntity<?> getMyTracks(@AuthenticationPrincipal KairoPrincipal principal) {
        return ResponseEntity.ok(getMyTracks.execute(principal.userId()));
    }

    @GetMapping("/tracks/{trackId}")
    public ResponseEntity<?> getById(@PathVariable UUID trackId,
                                     @AuthenticationPrincipal KairoPrincipal principal) {
        try {
            TrackWithChallengesResponse response = getTrackById.execute(principal.userId(), trackId);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/tracks/{trackId}/publish")
    public ResponseEntity<?> publish(@PathVariable UUID trackId,
                                     @AuthenticationPrincipal KairoPrincipal principal) {
        try {
            publishTrack.execute(principal.userId(), trackId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of("error", "Não tens permissão para publicar esta trilha."));
        }
    }

    @PostMapping("/challenges/{challengeId}/complete")
    public ResponseEntity<?> complete(@PathVariable UUID challengeId,
                                      @RequestBody CompleteChallengeRequest req,
                                      @AuthenticationPrincipal KairoPrincipal principal) {
        try {
            completeChallenge.execute(principal.userId(), challengeId,
                UUID.fromString(req.interactionId()));
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(409)
                .body(Map.of("error", "Sessão não encontrada. Inicia o desafio novamente."));
        }
    }
}

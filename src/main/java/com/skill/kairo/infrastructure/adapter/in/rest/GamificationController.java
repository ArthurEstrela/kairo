package com.skill.kairo.infrastructure.adapter.in.rest;

import com.skill.kairo.domain.model.gamification.GamificationProfile;
import com.skill.kairo.domain.repository.GamificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gamification")
public class GamificationController {

    // Como é apenas uma operação de LEITURA (Read), podemos injetar o repositório diretamente.
    // O rigor do Use Case é exigido sobretudo quando há alteração de estado (Write).
    private final GamificationRepository gamificationRepository;

    public GamificationController(GamificationRepository gamificationRepository) {
        this.gamificationRepository = gamificationRepository;
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<GamificationProfile> getProfile(@PathVariable UUID userId) {
        return gamificationRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
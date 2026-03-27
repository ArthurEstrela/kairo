package com.skill.kairo.infrastructure.adapter.in.rest;

import com.skill.kairo.domain.model.gamification.GamificationProfile;
import com.skill.kairo.domain.repository.GamificationRepository;
import com.skill.kairo.infrastructure.security.KairoPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gamification")
public class GamificationController {

    private final GamificationRepository gamificationRepository;

    public GamificationController(GamificationRepository gamificationRepository) {
        this.gamificationRepository = gamificationRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<GamificationProfile> getProfile(
            @AuthenticationPrincipal KairoPrincipal principal) {
        return gamificationRepository.findByUserId(principal.userId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

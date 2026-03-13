package com.skill.kairo.application.dto.response;

import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        String name,
        String email
) {}

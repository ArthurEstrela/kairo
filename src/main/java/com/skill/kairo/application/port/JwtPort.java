package com.skill.kairo.application.port;

import java.util.UUID;

public interface JwtPort {
    String generateToken(UUID userId, String email);
    UUID extractUserId(String token);
    String extractEmail(String token);
    boolean isTokenValid(String token);
}

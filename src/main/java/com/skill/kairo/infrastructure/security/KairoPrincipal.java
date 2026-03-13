package com.skill.kairo.infrastructure.security;

import java.util.UUID;

public record KairoPrincipal(UUID userId, String email) {}

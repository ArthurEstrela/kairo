package com.skill.kairo.application.usecase;

import com.skill.kairo.application.dto.response.CheckoutSessionResponse;

import java.util.UUID;

public interface CreateCheckoutSessionUseCase {
    CheckoutSessionResponse execute(UUID userId, String email);
}

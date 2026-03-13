package com.skill.kairo.application.usecase;

import com.skill.kairo.application.dto.request.RegisterRequest;
import com.skill.kairo.application.dto.response.AuthResponse;

public interface RegisterUseCase {
    AuthResponse execute(RegisterRequest request);
}

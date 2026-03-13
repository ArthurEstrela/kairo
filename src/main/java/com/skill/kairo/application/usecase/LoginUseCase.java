package com.skill.kairo.application.usecase;

import com.skill.kairo.application.dto.request.LoginRequest;
import com.skill.kairo.application.dto.response.AuthResponse;

public interface LoginUseCase {
    AuthResponse execute(LoginRequest request);
}

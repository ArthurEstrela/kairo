package com.skill.kairo.application.service;

import com.skill.kairo.application.dto.request.LoginRequest;
import com.skill.kairo.application.dto.response.AuthResponse;
import com.skill.kairo.application.port.JwtPort;
import com.skill.kairo.application.usecase.LoginUseCase;
import com.skill.kairo.domain.model.user.User;
import com.skill.kairo.domain.repository.UserRepository;

public class LoginService implements LoginUseCase {

    private final UserRepository userRepository;
    private final JwtPort jwtPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public LoginService(
            UserRepository userRepository,
            JwtPort jwtPort,
            PasswordEncoderPort passwordEncoderPort) {
        this.userRepository = userRepository;
        this.jwtPort = jwtPort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public AuthResponse execute(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        if (!passwordEncoderPort.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciais inválidas.");
        }

        String token = jwtPort.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail());
    }
}

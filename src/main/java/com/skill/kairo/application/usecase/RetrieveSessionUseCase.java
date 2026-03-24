package com.skill.kairo.application.usecase;

import com.skill.kairo.application.dto.response.SessionStatusResponse;

public interface RetrieveSessionUseCase {
    SessionStatusResponse execute(String sessionId);
}

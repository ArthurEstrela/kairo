package com.skill.kairo.application.usecase;

import com.skill.kairo.application.dto.response.GenerateTrackResponse;
import java.util.UUID;

public interface GenerateTrackUseCase {
    GenerateTrackResponse execute(UUID userId, String goal);
}

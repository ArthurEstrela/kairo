package com.skill.kairo.application.usecase;

import com.skill.kairo.application.dto.response.MyTracksResponse;
import java.util.UUID;

public interface GetMyTracksUseCase {
    MyTracksResponse execute(UUID userId);
}

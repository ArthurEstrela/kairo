package com.skill.kairo.application.usecase;

import java.util.UUID;

public interface PublishTrackUseCase {
    void execute(UUID requesterId, UUID trackId);
}

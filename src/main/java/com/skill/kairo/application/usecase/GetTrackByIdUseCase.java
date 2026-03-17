package com.skill.kairo.application.usecase;

import com.skill.kairo.application.dto.response.TrackWithChallengesResponse;
import java.util.UUID;

public interface GetTrackByIdUseCase {
    TrackWithChallengesResponse execute(UUID requesterId, UUID trackId);
}

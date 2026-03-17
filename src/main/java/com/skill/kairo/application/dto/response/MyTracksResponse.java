package com.skill.kairo.application.dto.response;

import java.util.List;

public record MyTracksResponse(
        int totalCount,
        List<TrackWithChallengesResponse> tracks
) {}

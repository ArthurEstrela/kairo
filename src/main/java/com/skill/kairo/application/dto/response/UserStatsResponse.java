package com.skill.kairo.application.dto.response;

import java.util.List;

public record UserStatsResponse(
        long completedChallenges,
        List<DayActivity> weeklyActivity
) {
    public record DayActivity(String day, long count) {}
}

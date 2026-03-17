package com.skill.kairo.application.dto.response;

public record RecentActivityItem(
        String description,
        String timeAgo,
        int score
) {}

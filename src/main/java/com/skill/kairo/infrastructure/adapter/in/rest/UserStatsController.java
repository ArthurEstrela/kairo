package com.skill.kairo.infrastructure.adapter.in.rest;

import com.skill.kairo.application.dto.response.RecentActivityItem;
import com.skill.kairo.application.dto.response.UserStatsResponse;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.ChallengeEntity;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.InteractionEntity;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SpringDataChallengeRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SpringDataInteractionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserStatsController {

    private final SpringDataInteractionRepository interactionRepo;
    private final SpringDataChallengeRepository challengeRepo;

    public UserStatsController(SpringDataInteractionRepository interactionRepo,
                                SpringDataChallengeRepository challengeRepo) {
        this.interactionRepo = interactionRepo;
        this.challengeRepo = challengeRepo;
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<UserStatsResponse> getStats(@PathVariable UUID userId) {
        long total = interactionRepo.countByUserId(userId);
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        List<InteractionEntity> weeklyInteractions = interactionRepo.findByUserIdAndCreatedAtAfter(userId, sevenDaysAgo);

        ZoneId zone = ZoneId.of("UTC");
        List<UserStatsResponse.DayActivity> weeklyActivity = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            ZonedDateTime day = ZonedDateTime.now(zone).minusDays(i);
            String dayLabel = day.getDayOfWeek().getDisplayName(TextStyle.NARROW, Locale.forLanguageTag("pt-BR"));
            long count = weeklyInteractions.stream()
                    .filter(r -> r.getCreatedAt().atZone(zone).toLocalDate().equals(day.toLocalDate()))
                    .count();
            weeklyActivity.add(new UserStatsResponse.DayActivity(dayLabel, count));
        }

        return ResponseEntity.ok(new UserStatsResponse(total, weeklyActivity));
    }

    @GetMapping("/{userId}/recent-activity")
    public ResponseEntity<List<RecentActivityItem>> getRecentActivity(@PathVariable UUID userId) {
        List<InteractionEntity> interactions = interactionRepo
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 5));

        List<UUID> challengeIds = interactions.stream().map(InteractionEntity::getChallengeId).toList();
        Map<UUID, String> titleMap = challengeRepo.findAllById(challengeIds).stream()
                .collect(Collectors.toMap(ChallengeEntity::getId, ChallengeEntity::getTitle));

        List<RecentActivityItem> items = interactions.stream().map(i -> {
            String title = titleMap.getOrDefault(i.getChallengeId(), "Desafio");
            String timeAgo = formatTimeAgo(i.getCreatedAt());
            String description = i.getScore() >= 70
                    ? "Completou \"" + title + "\" com " + i.getScore() + "% de pontuação"
                    : "Tentou \"" + title + "\" — " + i.getScore() + " pts";
            return new RecentActivityItem(description, timeAgo, i.getScore());
        }).toList();

        return ResponseEntity.ok(items);
    }

    private String formatTimeAgo(Instant instant) {
        long minutes = ChronoUnit.MINUTES.between(instant, Instant.now());
        if (minutes < 60) return "há " + minutes + " min";
        long hours = ChronoUnit.HOURS.between(instant, Instant.now());
        if (hours < 24) return "há " + hours + " hora" + (hours > 1 ? "s" : "");
        long days = ChronoUnit.DAYS.between(instant, Instant.now());
        return "há " + days + " dia" + (days > 1 ? "s" : "");
    }
}

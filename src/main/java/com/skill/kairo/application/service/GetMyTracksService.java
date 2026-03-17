package com.skill.kairo.application.service;

import com.skill.kairo.application.dto.response.MyTracksResponse;
import com.skill.kairo.application.dto.response.TrackChallengeResponse;
import com.skill.kairo.application.dto.response.TrackWithChallengesResponse;
import com.skill.kairo.application.usecase.GetMyTracksUseCase;
import com.skill.kairo.domain.model.challenge.Challenge;
import com.skill.kairo.domain.model.challenge.config.RoleplayConfig;
import com.skill.kairo.domain.model.user.Skill;
import com.skill.kairo.domain.repository.ChallengeProgressRepository;
import com.skill.kairo.domain.repository.ChallengeRepository;
import com.skill.kairo.domain.repository.SkillRepository;

import java.util.*;

public class GetMyTracksService implements GetMyTracksUseCase {

    private static final int PAGE_SIZE = 50;

    private final SkillRepository skillRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeProgressRepository progressRepository;

    public GetMyTracksService(SkillRepository skillRepository,
                              ChallengeRepository challengeRepository,
                              ChallengeProgressRepository progressRepository) {
        this.skillRepository = skillRepository;
        this.challengeRepository = challengeRepository;
        this.progressRepository = progressRepository;
    }

    @Override
    public MyTracksResponse execute(UUID userId) {
        long total = skillRepository.countByCreatedByUserId(userId);
        List<Skill> skills = skillRepository.findByCreatedByUserId(userId, PAGE_SIZE);

        List<UUID> allChallengeIds = new ArrayList<>();
        Map<UUID, List<Challenge>> challengesBySkill = new LinkedHashMap<>();
        for (Skill skill : skills) {
            List<Challenge> challenges = challengeRepository.findBySkillId(skill.id());
            challengesBySkill.put(skill.id(), challenges);
            challenges.forEach(c -> allChallengeIds.add(c.getId()));
        }

        Map<UUID, Integer> bestScoreByChallenge = new HashMap<>();
        if (!allChallengeIds.isEmpty()) {
            progressRepository.findByUserIdAndChallengeIdIn(userId, allChallengeIds)
                .forEach(p -> bestScoreByChallenge.put(p.challengeId(), p.bestScore()));
        }

        List<TrackWithChallengesResponse> tracks = skills.stream().map(skill -> {
            List<Challenge> challenges = challengesBySkill.getOrDefault(skill.id(), List.of());
            List<TrackChallengeResponse> challengeResponses = buildChallengeResponses(challenges, bestScoreByChallenge);
            return new TrackWithChallengesResponse(
                skill.id().toString(), skill.name(), skill.description(), challengeResponses
            );
        }).toList();

        return new MyTracksResponse((int) total, tracks);
    }

    static List<TrackChallengeResponse> buildChallengeResponses(
            List<Challenge> challenges, Map<UUID, Integer> bestScoreByChallenge) {
        List<Challenge> sorted = challenges.stream()
            .sorted(Comparator.comparingInt(Challenge::getLevelOrder)).toList();

        List<TrackChallengeResponse> result = new ArrayList<>();
        int prevBest = 100; // first challenge always unlocked

        for (int i = 0; i < sorted.size(); i++) {
            Challenge c = sorted.get(i);
            int best = bestScoreByChallenge.getOrDefault(c.getId(), 0);
            int maxTurns = (c.getConfig() instanceof RoleplayConfig rc) ? rc.maxTurns() : 0;

            String status;
            if (i == 0) {
                status = best >= 60 ? "COMPLETED" : "ACTIVE";
            } else {
                boolean unlocked = prevBest >= 60;
                if (!unlocked) status = "LOCKED";
                else status = best >= 60 ? "COMPLETED" : "ACTIVE";
            }
            prevBest = best;

            result.add(new TrackChallengeResponse(
                c.getId().toString(), c.getTitle(), c.getXpReward(),
                c.getLevelOrder(), maxTurns, status, best
            ));
        }
        return result;
    }
}

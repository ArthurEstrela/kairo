package com.skill.kairo.application.service;

import com.skill.kairo.application.dto.response.TrackChallengeResponse;
import com.skill.kairo.application.dto.response.TrackWithChallengesResponse;
import com.skill.kairo.application.usecase.GetTrackByIdUseCase;
import com.skill.kairo.domain.model.user.Skill;
import com.skill.kairo.domain.repository.ChallengeProgressRepository;
import com.skill.kairo.domain.repository.ChallengeRepository;
import com.skill.kairo.domain.repository.SkillRepository;

import java.util.*;

public class GetTrackByIdService implements GetTrackByIdUseCase {

    private final SkillRepository skillRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeProgressRepository progressRepository;

    public GetTrackByIdService(SkillRepository skillRepository,
                               ChallengeRepository challengeRepository,
                               ChallengeProgressRepository progressRepository) {
        this.skillRepository = skillRepository;
        this.challengeRepository = challengeRepository;
        this.progressRepository = progressRepository;
    }

    @Override
    public TrackWithChallengesResponse execute(UUID requesterId, UUID trackId) {
        Skill skill = skillRepository.findById(trackId)
            .orElseThrow(() -> new NoSuchElementException("TRACK_NOT_FOUND"));

        UUID owner = skill.createdByUserId();
        if (owner == null || (!owner.equals(requesterId) && !skill.isPublic())) {
            throw new NoSuchElementException("TRACK_NOT_FOUND");
        }

        var challenges = challengeRepository.findBySkillId(trackId);
        var challengeIds = challenges.stream().map(c -> c.getId()).toList();

        Map<UUID, Integer> bestScoreByChallenge = new HashMap<>();
        if (!challengeIds.isEmpty()) {
            progressRepository.findByUserIdAndChallengeIdIn(requesterId, challengeIds)
                .forEach(p -> bestScoreByChallenge.put(p.challengeId(), p.bestScore()));
        }

        List<TrackChallengeResponse> challengeResponses =
            GetMyTracksService.buildChallengeResponses(challenges, bestScoreByChallenge);
        return new TrackWithChallengesResponse(
            skill.id().toString(), skill.name(), skill.description(), challengeResponses
        );
    }
}

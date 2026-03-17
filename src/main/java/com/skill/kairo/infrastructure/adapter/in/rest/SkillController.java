package com.skill.kairo.infrastructure.adapter.in.rest;

import com.skill.kairo.application.dto.response.ChallengeResponse;
import com.skill.kairo.application.dto.response.SkillResponse;
import com.skill.kairo.domain.model.challenge.Challenge;
import com.skill.kairo.domain.model.challenge.config.QuizConfig;
import com.skill.kairo.domain.model.challenge.config.RoleplayConfig;
import com.skill.kairo.domain.repository.ChallengeRepository;
import com.skill.kairo.domain.repository.SkillRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {

    private final SkillRepository skillRepository;
    private final ChallengeRepository challengeRepository;

    public SkillController(SkillRepository skillRepository, ChallengeRepository challengeRepository) {
        this.skillRepository = skillRepository;
        this.challengeRepository = challengeRepository;
    }

    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        List<SkillResponse> skills = skillRepository.findAll()
                .stream()
                .map(skill -> new SkillResponse(skill.getId(), skill.getName(), skill.getDescription(), skill.getDifficultyLevel()))
                .toList();
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{skillId}/challenges")
    public ResponseEntity<List<ChallengeResponse>> getChallengesBySkill(@PathVariable UUID skillId) {
        skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Habilidade não encontrada."));

        List<ChallengeResponse> challenges = challengeRepository.findBySkillId(skillId)
                .stream()
                .map(this::toChallengeResponse)
                .toList();
        return ResponseEntity.ok(challenges);
    }

    private ChallengeResponse toChallengeResponse(Challenge challenge) {
        String type = switch (challenge.getConfig()) {
            case RoleplayConfig ignored -> "ROLEPLAY";
            case QuizConfig ignored -> "QUIZ";
        };
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getSkillId(),
                challenge.getTitle(),
                challenge.getXpReward(),
                challenge.getLevelOrder(),
                type
        );
    }
}

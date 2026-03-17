package com.skill.kairo.application.service;

import com.skill.kairo.application.usecase.PublishTrackUseCase;
import com.skill.kairo.domain.model.user.Skill;
import com.skill.kairo.domain.repository.SkillRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

public class PublishTrackService implements PublishTrackUseCase {

    private final SkillRepository skillRepository;

    public PublishTrackService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Override
    public void execute(UUID requesterId, UUID trackId) {
        Skill skill = skillRepository.findById(trackId)
            .orElseThrow(() -> new NoSuchElementException("TRACK_NOT_FOUND"));
        if (!requesterId.equals(skill.createdByUserId())) {
            throw new SecurityException("FORBIDDEN");
        }
        Skill published = new Skill(skill.id(), skill.name(), skill.description(),
            skill.difficultyLevel(), skill.createdByUserId(), true);
        skillRepository.save(published);
    }
}

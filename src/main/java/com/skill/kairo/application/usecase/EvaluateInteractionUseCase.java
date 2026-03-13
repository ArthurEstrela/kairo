package com.skill.kairo.application.usecase;

import com.skill.kairo.application.command.EvaluateInteractionCommand;
import com.skill.kairo.application.dto.response.InteractionResultResponse;

public interface EvaluateInteractionUseCase {
    InteractionResultResponse execute(EvaluateInteractionCommand command);
}
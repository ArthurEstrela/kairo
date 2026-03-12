package com.skill.kairo.application.usecase;

import com.skill.kairo.application.dto.request.SubmitInteractionRequest;
import com.skill.kairo.application.dto.response.InteractionResultResponse;

public interface EvaluateInteractionUseCase {
    InteractionResultResponse execute(SubmitInteractionRequest request);
}
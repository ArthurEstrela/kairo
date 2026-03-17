package com.skill.kairo.application.port;

import com.skill.kairo.domain.model.challenge.InteractionScore;
import java.util.List;
import java.util.function.Consumer;

public interface AIPort {
    String generateResponse(String systemPrompt, String userInput);
    void generateStreamingResponse(String systemPrompt, String userInput, Consumer<String> chunkConsumer);

    /**
     * Multi-turn evaluation. conversationHistory = flat list alternating [aiMsg, userMsg, aiMsg, ...]
     */
    InteractionScore evaluateInteraction(String systemPrompt, List<String> conversationHistory);

    /**
     * JSON mode track generation. Returns raw JSON string. Throws RuntimeException on Gemini error.
     */
    String generateStructuredTrack(String prompt);
}

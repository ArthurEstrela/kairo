package com.skill.kairo.application.port;

import com.skill.kairo.domain.model.challenge.InteractionScore;
import java.util.List;
import java.util.function.Consumer;

public interface AIPort {
    String generateResponse(String systemPrompt, String userInput);
    /**
     * Streams a response chunk by chunk via {@code chunkConsumer}.
     * If the stream fails, a {@code RuntimeException} propagates through the calling thread.
     * The consumer will not receive any error notification.
     *
     * @throws NullPointerException if systemPrompt or onChunk is null
     */
    void generateStreamingResponse(String systemPrompt, String userInput, Consumer<String> chunkConsumer);

    /**
     * Multi-turn evaluation. {@code conversationHistory} is a flat list alternating
     * [aiMsg, userMsg, aiMsg, userMsg, ...] where AI messages are at even indices (0, 2, ...)
     * and user messages are at odd indices. The list must have at least 1 element (the last user
     * message). An odd-length list (ending on a user message) is the normal case for evaluation.
     *
     * @throws IllegalArgumentException if {@code conversationHistory} is empty
     * @throws NullPointerException if systemPrompt or conversationHistory is null
     */
    InteractionScore evaluateInteraction(String systemPrompt, List<String> conversationHistory);

    /**
     * JSON mode track generation. Returns raw JSON string. Throws RuntimeException on Gemini error.
     */
    String generateStructuredTrack(String prompt);
}

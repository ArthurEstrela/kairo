package com.skill.kairo.domain.exception;

public class TrackGenerationLimitException extends RuntimeException {
    public TrackGenerationLimitException() {
        super("Atingiste o limite mensal de trilhas. Faz upgrade para Premium.");
    }
}

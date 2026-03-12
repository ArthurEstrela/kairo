package com.skill.kairo.domain.exception;

public class OutOfLivesException extends RuntimeException {
    
    public OutOfLivesException(String message) {
        super(message);
    }
}
package com.skill.kairo.domain.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("O email '" + email + "' já está registado.");
    }
}

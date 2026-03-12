package com.skill.kairo.domain.model.gamification;

import com.skill.kairo.domain.exception.OutOfLivesException;

public record Lives(int current, int max) {
    public Lives {
        if (current < 0) {
            throw new IllegalArgumentException("Vidas atuais não podem ser negativas.");
        }
        if (max <= 0) {
            throw new IllegalArgumentException("O máximo de vidas deve ser maior que zero.");
        }
        if (current > max) {
            throw new IllegalArgumentException("Vidas atuais não podem exceder o máximo.");
        }
    }

    public boolean isZero() {
        return current == 0;
    }

    public Lives decrement() {
        if (isZero()) {
            throw new OutOfLivesException("O usuário não possui vidas suficientes.");
        }
        return new Lives(current - 1, max);
    }

    public Lives restoreFull() {
        return new Lives(max, max);
    }
}
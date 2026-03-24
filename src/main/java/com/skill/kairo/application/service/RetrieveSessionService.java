package com.skill.kairo.application.service;

import com.skill.kairo.application.dto.response.SessionStatusResponse;
import com.skill.kairo.application.port.PaymentPort;
import com.skill.kairo.application.usecase.RetrieveSessionUseCase;

public class RetrieveSessionService implements RetrieveSessionUseCase {

    private final PaymentPort paymentPort;

    public RetrieveSessionService(PaymentPort paymentPort) {
        this.paymentPort = paymentPort;
    }

    @Override
    public SessionStatusResponse execute(String sessionId) {
        return paymentPort.retrieveSession(sessionId);
    }
}

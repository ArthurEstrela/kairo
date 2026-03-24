package com.skill.kairo.application.service;

import com.skill.kairo.application.dto.response.CheckoutSessionResponse;
import com.skill.kairo.application.port.PaymentPort;
import com.skill.kairo.application.usecase.CreateCheckoutSessionUseCase;

import java.util.UUID;

public class CreateCheckoutSessionService implements CreateCheckoutSessionUseCase {

    private final PaymentPort paymentPort;
    private final String priceId;
    private final String returnUrlTemplate;

    public CreateCheckoutSessionService(PaymentPort paymentPort,
                                        String priceId,
                                        String returnUrlTemplate) {
        this.paymentPort = paymentPort;
        this.priceId = priceId;
        this.returnUrlTemplate = returnUrlTemplate;
    }

    @Override
    public CheckoutSessionResponse execute(UUID userId, String email) {
        String returnUrl = returnUrlTemplate + "?session_id={CHECKOUT_SESSION_ID}";
        return paymentPort.createEmbeddedSession(userId, email, priceId, returnUrl);
    }
}

package com.skill.kairo.application.dto;

import java.time.Instant;

/**
 * DTO que representa um evento de subscrição Stripe já verificado e parseado.
 * Mantém a camada de aplicação livre de dependências do Stripe SDK e Jackson.
 */
public record StripeSubscriptionEvent(
        String eventType,
        String stripeCustomerId,
        String stripeSubscriptionId,
        Instant currentPeriodEnd,
        String kairoUserId          // nullable — populated only for checkout.session.completed
) {}

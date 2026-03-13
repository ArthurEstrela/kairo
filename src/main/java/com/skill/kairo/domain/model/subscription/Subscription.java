package com.skill.kairo.domain.model.subscription;

import java.time.Instant;
import java.util.UUID;

public class Subscription {

    private final UUID id;
    private final UUID userId;
    private String stripeCustomerId;
    private String stripeSubscriptionId;
    private SubscriptionPlan plan;
    private boolean active;
    private Instant currentPeriodEnd;

    public Subscription(UUID id, UUID userId, String stripeCustomerId, String stripeSubscriptionId,
                        SubscriptionPlan plan, boolean active, Instant currentPeriodEnd) {
        if (userId == null) throw new IllegalArgumentException("A assinatura precisa pertencer a um usuário.");
        if (plan == null) throw new IllegalArgumentException("O plano de assinatura é obrigatório.");

        this.id = id;
        this.userId = userId;
        this.stripeCustomerId = stripeCustomerId;
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.plan = plan;
        this.active = active;
        this.currentPeriodEnd = currentPeriodEnd;
    }

    public static Subscription createDefaultFreemium(UUID userId) {
        return new Subscription(UUID.randomUUID(), userId, null, null,
                SubscriptionPlan.FREEMIUM, true, Instant.MAX);
    }

    // --- COMPORTAMENTOS DO SAAS ---

    public void upgradeToPremium(String stripeCustomerId, String stripeSubscriptionId, Instant periodEnd) {
        this.plan = SubscriptionPlan.PREMIUM;
        this.stripeCustomerId = stripeCustomerId;
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.active = true;
        this.currentPeriodEnd = periodEnd;
    }

    public void cancel() {
        this.active = false;
    }

    public void renew(Instant newPeriodEnd) {
        if (!this.active) this.active = true;
        this.currentPeriodEnd = newPeriodEnd;
    }

    public boolean hasPremiumAccess(Instant now) {
        if (this.plan == SubscriptionPlan.FREEMIUM) return false;
        return this.active && now.isBefore(this.currentPeriodEnd);
    }

    // --- GETTERS ---
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getStripeCustomerId() { return stripeCustomerId; }
    public String getStripeSubscriptionId() { return stripeSubscriptionId; }
    public SubscriptionPlan getPlan() { return plan; }
    public boolean isActive() { return active; }
    public Instant getCurrentPeriodEnd() { return currentPeriodEnd; }
}

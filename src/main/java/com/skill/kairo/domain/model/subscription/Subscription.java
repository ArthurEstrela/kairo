package com.skill.kairo.domain.model.subscription;

import java.time.Instant;
import java.util.UUID;

public class Subscription {

    private final UUID id;
    private final UUID userId;
    private String stripeCustomerId;
    private SubscriptionPlan plan;
    private boolean active;
    private Instant currentPeriodEnd;

    public Subscription(UUID id, UUID userId, String stripeCustomerId, SubscriptionPlan plan, boolean active, Instant currentPeriodEnd) {
        if (userId == null) {
            throw new IllegalArgumentException("A assinatura precisa pertencer a um usuário.");
        }
        if (plan == null) {
            throw new IllegalArgumentException("O plano de assinatura é obrigatório.");
        }

        this.id = id;
        this.userId = userId;
        this.stripeCustomerId = stripeCustomerId;
        this.plan = plan;
        this.active = active;
        this.currentPeriodEnd = currentPeriodEnd;
    }

    // Cria uma assinatura gratuita padrão para novos usuários
    public static Subscription createDefaultFreemium(UUID userId) {
        return new Subscription(
                UUID.randomUUID(), 
                userId, 
                null, 
                SubscriptionPlan.FREEMIUM, 
                true, 
                Instant.MAX // Freemium não expira no tempo, é limitado por recursos/vidas
        );
    }

    // --- COMPORTAMENTOS DO SAAS ---

    public void upgradeToPremium(String newStripeCustomerId, Instant periodEnd) {
        this.plan = SubscriptionPlan.PREMIUM;
        this.stripeCustomerId = newStripeCustomerId;
        this.active = true;
        this.currentPeriodEnd = periodEnd;
    }

    public void cancel() {
        this.active = false;
        // Dependendo da regra, você pode manter ativo até o currentPeriodEnd, 
        // mas marcamos a intenção de cancelamento aqui.
    }

    public void renew(Instant newPeriodEnd) {
        if (!this.active && this.plan != SubscriptionPlan.FREEMIUM) {
            this.active = true;
        }
        this.currentPeriodEnd = newPeriodEnd;
    }

    // O domínio sabe responder se o cara tem acesso premium válido neste exato momento
    public boolean hasPremiumAccess(Instant now) {
        if (this.plan == SubscriptionPlan.FREEMIUM) return false;
        return this.active && now.isBefore(this.currentPeriodEnd);
    }

    // --- GETTERS ---
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getStripeCustomerId() { return stripeCustomerId; }
    public SubscriptionPlan getPlan() { return plan; }
    public boolean isActive() { return active; }
    public Instant getCurrentPeriodEnd() { return currentPeriodEnd; }
}
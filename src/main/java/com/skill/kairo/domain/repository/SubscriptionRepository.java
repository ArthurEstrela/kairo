package com.skill.kairo.domain.repository;

import com.skill.kairo.domain.model.subscription.Subscription;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {
    void save(Subscription subscription);
    Optional<Subscription> findByUserId(UUID userId);
    Optional<Subscription> findByStripeCustomerId(String stripeCustomerId);
}

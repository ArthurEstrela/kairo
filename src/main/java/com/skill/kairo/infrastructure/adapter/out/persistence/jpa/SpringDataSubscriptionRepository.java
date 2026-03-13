package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataSubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {
    Optional<SubscriptionEntity> findByUserId(UUID userId);
    Optional<SubscriptionEntity> findByStripeCustomerId(String stripeCustomerId);
}

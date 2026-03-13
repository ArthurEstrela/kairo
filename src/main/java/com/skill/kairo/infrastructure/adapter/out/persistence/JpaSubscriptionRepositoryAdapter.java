package com.skill.kairo.infrastructure.adapter.out.persistence;

import com.skill.kairo.domain.model.subscription.Subscription;
import com.skill.kairo.domain.model.subscription.SubscriptionPlan;
import com.skill.kairo.domain.repository.SubscriptionRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SpringDataSubscriptionRepository;
import com.skill.kairo.infrastructure.adapter.out.persistence.jpa.SubscriptionEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaSubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final SpringDataSubscriptionRepository springDataRepository;

    public JpaSubscriptionRepositoryAdapter(SpringDataSubscriptionRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void save(Subscription subscription) {
        SubscriptionEntity entity = new SubscriptionEntity(
                subscription.getId(),
                subscription.getUserId(),
                subscription.getStripeCustomerId(),
                subscription.getStripeSubscriptionId(),
                subscription.getPlan().name(),
                subscription.isActive(),
                subscription.getCurrentPeriodEnd()
        );
        springDataRepository.save(entity);
    }

    @Override
    public Optional<Subscription> findByUserId(UUID userId) {
        return springDataRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public Optional<Subscription> findByStripeCustomerId(String stripeCustomerId) {
        return springDataRepository.findByStripeCustomerId(stripeCustomerId).map(this::toDomain);
    }

    private Subscription toDomain(SubscriptionEntity entity) {
        return new Subscription(
                entity.getId(),
                entity.getUserId(),
                entity.getStripeCustomerId(),
                entity.getStripeSubscriptionId(),
                SubscriptionPlan.valueOf(entity.getPlan()),
                entity.isActive(),
                entity.getCurrentPeriodEnd()
        );
    }
}

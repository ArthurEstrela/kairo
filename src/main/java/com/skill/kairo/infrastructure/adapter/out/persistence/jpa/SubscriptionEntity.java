package com.skill.kairo.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionEntity {

    @Id
    private UUID id;

    private UUID userId;
    private String stripeCustomerId;
    private String stripeSubscriptionId;
    private String plan;
    private boolean active;
    private Instant currentPeriodEnd;
}

package com.skill.kairo.infrastructure.adapter.out.event;

import com.skill.kairo.application.port.EventPublisherPort;
import com.skill.kairo.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPublisherAdapter implements EventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        // Pega no evento purista do nosso DDD e entrega-o ao barramento do Spring Boot
        System.out.println("📢 A publicar evento de domínio: " + event.getClass().getSimpleName());
        applicationEventPublisher.publishEvent(event);
    }
}
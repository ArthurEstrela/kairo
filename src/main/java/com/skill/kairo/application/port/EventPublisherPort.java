package com.skill.kairo.application.port;

import com.skill.kairo.domain.event.DomainEvent;

public interface EventPublisherPort {
    // Dispara o evento para os listeners (como envio de emails, websockets, etc.)
    void publish(DomainEvent event);
}
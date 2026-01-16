package com.lifeos.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}

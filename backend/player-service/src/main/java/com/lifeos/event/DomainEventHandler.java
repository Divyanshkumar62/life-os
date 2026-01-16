package com.lifeos.event;

public interface DomainEventHandler<T extends DomainEvent> {
    boolean supports(DomainEvent event);
    void handle(T event);
}

package com.santiago.base.core.event;

public record DomainEvent<T>(
        EventType type,
        T payload
) {}

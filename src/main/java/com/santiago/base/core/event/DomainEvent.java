package com.santiago.base.core.event;

public record DomainEvent<T, U>(
        T type,
        U payload
) {}

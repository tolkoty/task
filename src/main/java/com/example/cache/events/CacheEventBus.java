package com.example.cache.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class CacheEventBus {
    private final ApplicationEventPublisher publisher;

    public CacheEventBus(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(CacheEvent event) {
        publisher.publishEvent(event);
    }
}

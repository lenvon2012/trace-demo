package com.trace.demo.service.redis.pubsub;

public interface CustomerInfoPublisher {
    void publish(Object message);
}

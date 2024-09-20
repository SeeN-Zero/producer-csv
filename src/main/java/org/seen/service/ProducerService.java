package org.seen.service;

import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProducerService {
    @Inject
    @Channel("file-channel")
    Emitter<String> emitter;

    public void sendMessage(String message) {
        emitter.send(message);
    }
}

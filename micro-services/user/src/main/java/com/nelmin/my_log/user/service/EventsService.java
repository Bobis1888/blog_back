package com.nelmin.my_log.user.service;

import com.nelmin.my_log.user.dto.kafka.AuthEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventsService {

    public final static String RESET_PASSWORD_EVENT_NAME = "reset-password";
    public final static String REGISTRATION_EVENT_NAME = "registration";
    public final static String OAUTH_REGISTRATION_EVENT_NAME = "oauth-registration";
    public final static String BLOCK_EVENT_NAME = "block";

    @Value("${auth.events.topic:auth-events}")
    private String eventsTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEvent(@NonNull String type, @NonNull Map<String, String> payload) {
        var event = new AuthEvent();
        event.setType(type);
        event.setPayload(payload);
        kafkaTemplate.send(eventsTopic, event).whenComplete((producerRecord, e) -> {
            if (e != null) {
                log.error("Failed to send event {}", event, e);
            }
        });
    }
}

package com.nelmin.my_log.email.service;

import com.nelmin.my_log.email.dto.kafka.AuthEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@KafkaListener(topics = "auth-events")
@RequiredArgsConstructor
public class EventHandlerService {

    private final AuthEventService mailService;

    @KafkaHandler
    public void handleAuthEvents(AuthEvent event) {

        var payload = event.getPayload();
        var type = event.getType();

        switch (type) {
            case "registration":
                mailService.sendConfirmEmail(payload.get("email"), payload.get("uuid"));
                break;
            case "reset-password":
                mailService.sendResetEmail(payload.get("email"), payload.get("uuid"));
                break;
            case "oauth-registration":
                mailService.sendOauthEmail(payload.get("email"));
                break;
            case "block":
                mailService.sendBlockEmail(payload);
                break;
            default:
                break;
        }
    }
}

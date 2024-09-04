package com.nelmin.my_log.notification.service.handler;

import com.nelmin.my_log.notification.dto.kafka.ContentEvent;
import com.nelmin.my_log.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@KafkaListener(topics = "content-events")
@RequiredArgsConstructor
public class ContentEventHandlerService {

    private final NotificationService notificationService;

    @KafkaHandler
    public void handleAuthEvents(ContentEvent event) {
        log.info("Content event received: {}", event.getType());
        notificationService.createNotification(event);
    }
}

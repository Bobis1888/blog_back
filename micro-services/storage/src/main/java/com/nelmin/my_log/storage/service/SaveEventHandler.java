package com.nelmin.my_log.storage.service;

import com.nelmin.my_log.storage.dto.SaveEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@KafkaListener(topics = "save-events")
@RequiredArgsConstructor
public class SaveEventHandler {

    @KafkaHandler
    public void handle(SaveEvent event) {
        log.info("Save event received: {}", event);
    }
}

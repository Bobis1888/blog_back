package com.nelmin.my_log.storage.service;

import com.nelmin.my_log.storage.dto.FileType;
import com.nelmin.my_log.storage.dto.kafka.UpdateImages;
import com.nelmin.my_log.storage.model.RedisStorage;
import com.nelmin.my_log.storage.model.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@KafkaListener(topics = "image-events")
public class ImageEventsHandler {

    private final RedisStorage.Repo tmpStorage;
    private final Storage.Repo longStorage;

    @KafkaHandler
    @Transactional
    public void handle(UpdateImages event) {
        log.info("Update images event received: {}", event);
        List<Storage> saveList = new ArrayList<>();

        if (event.save() != null) {

            event.save().forEach(it -> {
                var tmp = tmpStorage.findById(it);
                tmp.ifPresent(itt -> {

                    if (itt.getUserId().equals(event.userId())) {
                        var storage = new Storage();
                        storage.setUserId(itt.getUserId());
                        storage.setUuid(itt.getId());
                        storage.setContentType(itt.getContentType());
                        storage.setFile(itt.getFile());
                        storage.setType(FileType.IMAGE);
                        saveList.add(storage);
                        tmpStorage.deleteById(itt.getId());
                    }
                });
            });
        }

        try {
            longStorage.saveAll(saveList);

            if (event.userId() != null) {
                longStorage.deleteByUserIdAndUuidIn(event.userId(), event.remove());
            } else {
                longStorage.deleteAllByUuidIn(event.remove());
            }
        } catch (Exception ex) {
            log.error("Error update images", ex);
        }
    }
}

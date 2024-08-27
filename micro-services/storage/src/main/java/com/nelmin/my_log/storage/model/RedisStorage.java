package com.nelmin.my_log.storage.model;

import lombok.Data;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Data
@RedisHash(value = "storage", timeToLive = 15 * 60L)
public class RedisStorage {
    private String id;
    private Long userId;
    private String contentType;
    private byte[] file;

    @Repository
    public interface Repo extends CrudRepository<RedisStorage, String> {
        Optional<RedisStorage> findByUuid(String uuid);
    }
}

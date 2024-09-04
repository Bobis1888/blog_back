package com.nelmin.my_log.storage.model;

import lombok.Data;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Data
@RedisHash(value = "storage", timeToLive = 60L * 60L)
public class RedisStorage {
    private String id;
    private Long userId;
    private String contentType;
    private byte[] file;

    @Repository
    public interface Repo extends CrudRepository<RedisStorage, String> { }
}

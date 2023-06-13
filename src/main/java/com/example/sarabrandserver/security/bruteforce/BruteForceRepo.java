package com.example.sarabrandserver.security.bruteforce;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class BruteForceRepo {
    private final String KEY = "BRUTE-FORCE-ENTITY";
    private final HashOperations<String, String, BruteForceEntity> hashOperations;

    public BruteForceRepo(RedisTemplate<String, Object> redisTemplate) {
        this.hashOperations = redisTemplate.opsForHash();
    }

    public void save(BruteForceEntity entity) {
        hashOperations.put(KEY, entity.getPrincipal(), entity);
    }

    public Optional<BruteForceEntity> findByPrincipal(String principal) {
        return Optional.ofNullable(this.hashOperations.get(KEY, principal));
    }

    public void update(BruteForceEntity entity) {
        save(entity);
    }

    public void delete(String principal) {
        findByPrincipal(principal).ifPresent(entity -> this.hashOperations.delete(this.KEY, principal));
    }

}

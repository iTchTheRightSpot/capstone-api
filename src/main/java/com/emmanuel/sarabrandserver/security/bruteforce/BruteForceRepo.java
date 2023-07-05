package com.emmanuel.sarabrandserver.security.bruteforce;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BruteForceRepo {
    private final Map<String, Object> mapOperation;

    public BruteForceRepo() {
        this.mapOperation = new ConcurrentHashMap<>();
    }

    public void save(BruteForceEntity entity) {
        mapOperation.put(entity.getPrincipal(), entity);
    }

    public Optional<BruteForceEntity> findByPrincipal(String principal) {
        return Optional.ofNullable((BruteForceEntity) this.mapOperation.get(principal));
    }

    public void update(BruteForceEntity entity) {
        save(entity);
    }

    public void delete(String principal) {
        findByPrincipal(principal).ifPresent(entity -> this.mapOperation.remove(principal));
    }

}

package com.emmanuel.sarabrandserver.auth.bruteforce;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class BruteForceRepo {

    private final Map<String, Object> mapOperation;

    public BruteForceRepo(@Qualifier(value = "mapOperation") Map<String, Object> mapOperation) {
        this.mapOperation = mapOperation;
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

package com.emmanuel.sarabrandserver.collection.service;

import com.emmanuel.sarabrandserver.collection.repository.CollectionRepository;
import com.emmanuel.sarabrandserver.collection.response.CollectionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientCollectionService {
    private final CollectionRepository collectionRepository;

    public ClientCollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public List<CollectionResponse> fetchAll() {
        return this.collectionRepository.fetchAllCollectionClient()
                .stream()
                .map(pojo -> new CollectionResponse(pojo.getCollection()))
                .toList();
    }
}

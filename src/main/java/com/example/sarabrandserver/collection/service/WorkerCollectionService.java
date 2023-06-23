package com.example.sarabrandserver.collection.service;

import com.example.sarabrandserver.collection.projection.CollectionPojo;
import com.example.sarabrandserver.collection.repository.CollectionRepository;
import com.example.sarabrandserver.collection.entity.ProductCollection;
import com.example.sarabrandserver.exception.CustomNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkerCollectionService {
    private final CollectionRepository collectionRepository;

    public WorkerCollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    /***
     * Returns a list of ProductCollection
     * @return List of CollectionPojo
     * */
    public List<CollectionPojo> fetchAll() {
        return this.collectionRepository.getAll();
    }

    public ProductCollection findByName(String name) {
        return this.collectionRepository.findByName(name)
                .orElseThrow(() -> new CustomNotFoundException(name + " does not exist"));
    }

    public void save (ProductCollection collection) {
        this.collectionRepository.save(collection);
    }

}

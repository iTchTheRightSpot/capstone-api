package com.emmanuel.sarabrandserver.collection.service;

import com.emmanuel.sarabrandserver.collection.dto.CollectionDTO;
import com.emmanuel.sarabrandserver.collection.entity.ProductCollection;
import com.emmanuel.sarabrandserver.collection.repository.CollectionRepository;
import com.emmanuel.sarabrandserver.collection.response.CollectionResponse;
import com.emmanuel.sarabrandserver.exception.CustomNotFoundException;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;

import static org.springframework.http.HttpStatus.CREATED;

@Service
public class WorkerCollectionService {
    private final CollectionRepository collectionRepository;
    private final CustomUtil customUtil;

    public WorkerCollectionService(CollectionRepository collectionRepository, CustomUtil customUtil) {
        this.collectionRepository = collectionRepository;
        this.customUtil = customUtil;
    }

    /** Returns a list of CollectionResponse. */
    public Page<CollectionResponse> fetchAll(int page, int size) {
        return this.collectionRepository.fetchAllCollection(PageRequest.of(page, size)) //
                .map(pojo -> CollectionResponse.builder()
                        .collection(pojo.getCollection())
                        .created(pojo.getCreated().getTime())
                        .modified(pojo.getModified() == null ? 0L : pojo.getModified().getTime())
                        .visible(pojo.getVisible())
                        .build()
                );
    }

    /**
     * Creates a ProductCollection object
     * @param dto of type CollectionDTO
     * @throws DuplicateException if collection name exists
     * @return ResponseEntity of type HttpStatus
     * */
    public ResponseEntity<?> create(CollectionDTO dto) {
        if (this.collectionRepository.findByName(dto.getName().trim()).isPresent()) {
            throw new DuplicateException(dto.getName() + " exists");
        }

        var date = this.customUtil.toUTC(new Date()).orElseGet(Date::new);
        var collection = ProductCollection.builder()
                .collection(dto.getName().trim())
                .createAt(date)
                .modifiedAt(null)
                .isVisible(dto.getVisible())
                .products(new HashSet<>())
                .build();

        this.collectionRepository.save(collection);
        return new ResponseEntity<>(CREATED);
    }

    public ProductCollection findByName(String name) {
        return this.collectionRepository.findByName(name)
                .orElseThrow(() -> new CustomNotFoundException(name + " does not exist"));
    }

    /** Needed in WorkerProductService class */
    public void save (ProductCollection collection) {
        this.collectionRepository.save(collection);
    }

}

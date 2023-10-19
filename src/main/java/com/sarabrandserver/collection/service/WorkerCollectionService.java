package com.sarabrandserver.collection.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.collection.dto.CollectionDTO;
import com.sarabrandserver.collection.dto.UpdateCollectionDTO;
import com.sarabrandserver.collection.entity.ProductCollection;
import com.sarabrandserver.collection.repository.CollectionRepository;
import com.sarabrandserver.collection.response.CollectionResponse;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.DuplicateException;
import com.sarabrandserver.product.response.ProductResponse;
import com.sarabrandserver.util.CustomUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class WorkerCollectionService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    @Value(value = "${spring.profiles.active}")
    private String ACTIVEPROFILE;

    private final CollectionRepository collectionRepository;
    private final CustomUtil customUtil;
    private final S3Service s3Service;

    public WorkerCollectionService(
            CollectionRepository collectionRepository,
            CustomUtil customUtil,
            S3Service s3Service
    ) {
        this.collectionRepository = collectionRepository;
        this.customUtil = customUtil;
        this.s3Service = s3Service;
    }

    /**
     * Returns a list of CollectionResponse.
     */
    public List<CollectionResponse> fetchAllCategories() {
        return this.collectionRepository
                .fetchAllCollection() //
                .stream() //
                .map(pojo -> CollectionResponse.builder()
                        .id(pojo.getUuid())
                        .collection(pojo.getCollection())
                        .created(pojo.getCreated().getTime())
                        .modified(pojo.getModified() == null ? 0L : pojo.getModified().getTime())
                        .visible(pojo.getVisible())
                        .build()
                )
                .toList();
    }

    public Page<ProductResponse> allProductsByCollection(String id, int page, int size) {
        boolean bool = this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage");
        return this.collectionRepository
                .allProductsByCollection(id, PageRequest.of(page, size))
                .map(pojo -> {
                    var url = this.s3Service.getPreSignedUrl(bool, this.BUCKET, pojo.getKey());

                    return ProductResponse.builder()
                            .id(pojo.getUuid())
                            .name(pojo.getName())
                            .price(pojo.getPrice())
                            .currency(pojo.getCurrency())
                            .imageUrl(url)
                            .build();
                });
    }

    /**
     * Creates a ProductCollection object
     *
     * @param dto of type CollectionDTO
     * @throws DuplicateException if collection name exists
     */
    public void create(CollectionDTO dto) {
        if (this.collectionRepository.findByName(dto.name().trim()).isPresent()) {
            throw new DuplicateException(dto.name() + " exists");
        }

        var date = this.customUtil.toUTC(new Date()).orElseGet(Date::new);
        var collection = ProductCollection.builder()
                .uuid(UUID.randomUUID().toString())
                .collection(dto.name().trim())
                .createAt(date)
                .modifiedAt(null)
                .isVisible(dto.visible())
                .products(new HashSet<>())
                .build();

        this.collectionRepository.save(collection);
    }

    /**
     * Method is responsible for updating a ProductCollection based on uuid.
     *
     * @param dto of type UpdateCollectionDTO
     * @throws DuplicateException when dto.name exists, and it is not associated to uuid
     */
    @Transactional
    public void update(UpdateCollectionDTO dto) {
        boolean bool = this.collectionRepository
                .duplicateCategoryForUpdate(dto.id().trim(), dto.name().trim()) > 0;

        if (bool) {
            throw new DuplicateException(dto.name() + " is a duplicate");
        }

        var date = this.customUtil.toUTC(new Date()).orElseGet(Date::new);

        this.collectionRepository
                .update(
                        date,
                        dto.name(),
                        dto.visible(),
                        dto.id().trim()
                );
    }

    /**
     * Deletes ProductCollection based on its uuid
     *
     * @param uuid unique to every ProductCollection
     * @throws CustomNotFoundException is thrown if ProductCollection does not exist
     */
    @Transactional
    public void delete(String uuid) {
        var collection = findByUuid(uuid);

        this.collectionRepository.delete(collection);
    }

    // Called in WorkerProductService
    public ProductCollection findByName(String name) {
        return this.collectionRepository.findByName(name)
                .orElseThrow(() -> new CustomNotFoundException(name + " does not exist"));
    }

    public ProductCollection findByUuid(String uuid) {
        return this.collectionRepository.findByUuid(uuid)
                .orElseThrow(() -> new CustomNotFoundException("ProductCollection does not exist"));
    }

}
package com.sarabrandserver.product.service;

import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.ResourceAttachedException;
import com.sarabrandserver.product.dto.SizeInventoryDTO;
import com.sarabrandserver.product.entity.ProductDetail;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductSkuService {

    private static final Logger log = LoggerFactory.getLogger(ProductSkuService.class);

    private final ProductSkuRepo repository;

    /**
     * Save {@link ProductSku}.
     */
    @Transactional
    public void save(SizeInventoryDTO[] arr, ProductDetail detail) {
        for (var dto : arr) {
            this.repository
                    .save(
                            ProductSku.builder()
                                    .sku(UUID.randomUUID().toString())
                                    .size(dto.size())
                                    .inventory(dto.qty())
                                    .productDetail(detail)
                                    .orderDetails(new HashSet<>())
                                    .reservations(new HashSet<>())
                                    .cartItems(new HashSet<>())
                                    .build()
                    );
        }
    }

    /**
     * Deletes ProductSku by sku.
     *
     * @throws ResourceAttachedException if {@link ProductSku}
     * has children entities attached to it.
     * */
    @Transactional
    public void delete(final String sku) {
        try {
            this.repository.deleteProductSkuBySku(sku);
        } catch (DataIntegrityViolationException e) {
            log.error("tried deleting a category with children attached {}", e.getMessage());
            throw new ResourceAttachedException("resource(s) attached to product");
        }
    }

    public ProductSku productSkuBySku(final String sku) {
        return this.repository
                .findBySku(sku)
                .orElseThrow(() -> new CustomNotFoundException("sku %s does not exist".formatted(sku)));
    }

}

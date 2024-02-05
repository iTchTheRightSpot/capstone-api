package com.sarabrandserver.product.service;

import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.product.dto.SizeInventoryDTO;
import com.sarabrandserver.product.entity.ProductDetail;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductSKUService {

    private final ProductSkuRepo productSkuRepo;

    /**
     * Save Product sku. Look in db diagram in read me in case of confusion
     */
    @Transactional
    public void save(SizeInventoryDTO[] arr, ProductDetail detail) {
        for (var dto : arr) {
            this.productSkuRepo
                    .save(
                            ProductSku.builder()
                                    .sku(UUID.randomUUID().toString())
                                    .size(dto.size())
                                    .inventory(dto.qty())
                                    .productDetail(detail)
                                    .orderDetail(new HashSet<>())
                                    .reservations(new HashSet<>())
                                    .cartItems(new HashSet<>())
                                    .build()
                    );
        }
    }

    /**
     * Deletes ProductSku by sku.
     *
     * @throws org.springframework.dao.DataIntegrityViolationException if {@code ProductSku}
     * has children entities attached to it.
     * */
    @Transactional
    public void delete(final String sku) {
        this.productSkuRepo.deleteProductSkuBySku(sku);
    }

    public int itemBeenBought(final String sku) {
        return this.productSkuRepo.skuHasBeenPurchased(sku);
    }

    public ProductSku productSkuBySKU(final String sku) {
        return this.productSkuRepo
                .findBySku(sku)
                .orElseThrow(() -> new CustomNotFoundException("sku %s does not exist".formatted(sku)));
    }

}

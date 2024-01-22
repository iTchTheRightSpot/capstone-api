package com.sarabrandserver.product.service;

import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.ResourceAttachedException;
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
                                    .build()
                    );
        }
    }

    /**
     * Deletes ProductSKU
     * */
    @Transactional
    public void delete(final String sku) {
        if (this.productSkuRepo.skuContainsInUserCart(sku) > 0 || this.productSkuRepo.skuHasBeenPurchased(sku) > 0) {
            throw new ResourceAttachedException("cannot delete item as it contains a users cart or order history");
        }

        var obj = productSkuBySKU(sku);
        this.productSkuRepo.delete(obj);
    }

    public int itemBeenBought(final String sku) {
        return this.productSkuRepo.skuHasBeenPurchased(sku);
    }

    public ProductSku productSkuBySKU(final String sku) {
        return this.productSkuRepo
                .findBySku(sku)
                .orElseThrow(() -> new CustomNotFoundException("SKU %s does not exist".formatted(sku)));
    }

}

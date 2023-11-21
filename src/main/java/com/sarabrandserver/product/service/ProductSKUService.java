package com.sarabrandserver.product.service;

import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.product.dto.SizeInventoryDTO;
import com.sarabrandserver.product.entity.ProductDetail;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.repository.ProductSkuRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        for (SizeInventoryDTO dto : arr) {
            var sku = new ProductSku(UUID.randomUUID().toString(), dto.size(), dto.qty(), detail);
            this.productSkuRepo.save(sku);
        }
    }

    /**
     * Deletes ProductSKU
     * */
    @Transactional
    public void delete(final String sku) {
        // TODO validate if it contains in order or user session before deleting
        var obj = productSkuBySKU(sku);
        this.productSkuRepo.delete(obj);
    }

    public ProductSku productSkuBySKU(String sku) {
        return this.productSkuRepo
                .findBySku(sku)
                .orElseThrow(() -> new CustomNotFoundException("SKU %s does not exist".formatted(sku)));
    }

}

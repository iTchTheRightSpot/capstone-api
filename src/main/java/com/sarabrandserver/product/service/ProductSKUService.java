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
        if (this.productSkuRepo.itemContainsCart(sku) > 0 || this.productSkuRepo.itemBeenBought(sku) > 0) {
            throw new ResourceAttachedException("cannot delete item as it contains a users cart or order history");
        }

        var obj = productSkuBySKU(sku);
        this.productSkuRepo.delete(obj);
    }

    public int itemBeenBought(final String sku) {
        return this.productSkuRepo.itemBeenBought(sku);
    }

    public ProductSku productSkuBySKU(String sku) {
        return this.productSkuRepo
                .findBySku(sku)
                .orElseThrow(() -> new CustomNotFoundException("SKU %s does not exist".formatted(sku)));
    }

}

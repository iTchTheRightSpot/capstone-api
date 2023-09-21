package com.emmanuel.sarabrandserver.product.service;

import com.emmanuel.sarabrandserver.exception.CustomNotFoundException;
import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import com.emmanuel.sarabrandserver.product.entity.ProductSku;
import com.emmanuel.sarabrandserver.product.repository.ProductSkuRepo;
import com.emmanuel.sarabrandserver.product.util.SizeInventoryDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductSKUService {

    private final ProductSkuRepo productSkuRepo;

    public ProductSKUService(ProductSkuRepo productSkuRepo) {
        this.productSkuRepo = productSkuRepo;
    }

    /**
     * Save Product sku. Look in db diagram in read me in case of confusion
     */
    public void saveProductSKUs(SizeInventoryDTO[] dto, ProductDetail detail) {
        for (SizeInventoryDTO sizeDto : dto) {
            var sku = ProductSku.builder()
                    .productDetail(detail)
                    .sku(UUID.randomUUID().toString())
                    .size(sizeDto.getSize())
                    .inventory(sizeDto.getQty())
                    .build();
            this.productSkuRepo.save(sku);
        }
    }

    /**
     * Deletes ProductSKU
     * */
    @Transactional
    public void deleteProductSKU(final String sku) {
        var obj = this.productSkuRepo
                .findBySku(sku)
                .orElseThrow(() -> new CustomNotFoundException("SKU %s does not exist".formatted(sku)));

        this.productSkuRepo.delete(obj);
    }

}

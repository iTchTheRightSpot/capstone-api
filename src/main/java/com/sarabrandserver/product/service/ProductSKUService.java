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
    public void saveProductSKUs(SizeInventoryDTO[] dto, ProductDetail detail) {
        for (SizeInventoryDTO sizeDto : dto) {
            var sku = ProductSku.builder()
                    .productDetail(detail)
                    .sku(UUID.randomUUID().toString())
                    .size(sizeDto.size())
                    .inventory(sizeDto.qty())
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

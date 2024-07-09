package dev.webserver.product;

import dev.webserver.exception.CustomNotFoundException;
import dev.webserver.exception.ResourceAttachedException;
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
@Transactional(rollbackFor = Exception.class)
public class ProductSkuService {

    private static final Logger log = LoggerFactory.getLogger(ProductSkuService.class);

    private final ProductSkuRepository repository;

    /**
     * Saves {@link ProductSku} based on {@link SizeInventoryDTO} array.
     */
    public void save(SizeInventoryDTO[] arr, ProductDetail detail) {
        for (var dto : arr) {
            this.repository.save(
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
     * Deletes a {@link ProductSku} by sku.
     *
     * @throws ResourceAttachedException if {@link ProductSku}
     * has children entities attached to it.
     * */
    public void delete(final String sku) {
        try {
            this.repository.deleteProductSkuBySku(sku);
        } catch (DataIntegrityViolationException e) {
            log.error("resources attached to ProductSku {}", e.getMessage());
            throw new ResourceAttachedException("resource(s) attached to product");
        }
    }

    public ProductSku productSkuBySku(final String sku) {
        return this.repository
                .productSkuBySku(sku)
                .orElseThrow(() -> new CustomNotFoundException("sku %s does not exist".formatted(sku)));
    }

}

package dev.webserver.product;

import dev.webserver.AbstractEnvironment;
import dev.webserver.exception.CustomNotFoundException;
import dev.webserver.exception.DuplicateException;
import dev.webserver.util.CustomUtil;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
class EmployeeProductDetailService extends AbstractEnvironment {

    private final ProductDetailRepository detailRepo;
    private final ProductSkuService skuService;
    private final ProductRepository productRepository;
    private final ProductImageService productImageService;

    protected EmployeeProductDetailService(final Environment environment, final ProductDetailRepository detailRepo, final ProductSkuService skuService, final ProductRepository productRepository, final ProductImageService productImageService) {
        super(environment);
        this.detailRepo = detailRepo;
        this.skuService = skuService;
        this.productRepository = productRepository;
        this.productImageService = productImageService;
    }

    /**
     * Retrieves {@link ProductDetail} asynchronously by the specified {@link Product} uuid.
     * Each {@link ProductDetail} consists of various attributes such as visibility, color,
     * URLs, and variants.
     *
     * @param uuid The uuid of the {@link Product} for which details are to be retrieved.
     * @return A list of {@link DetailResponse} objects, representing the {@link ProductDetail}.
     * Each {@link DetailResponse} object encapsulates information such as visibility, color, URLs, and variants.
     */
    public List<DetailResponse> productDetailsByProductUuid(final String uuid) {
        final var futures = detailRepo
                .productDetailsByProductUuidAdminFront(uuid)
                .stream()
                .map(pojo -> (Supplier<DetailResponse>) () -> {
                    final var req = Arrays
                            .stream(pojo.imageKey().split(","))
                            .map(key -> (Supplier<String>) () -> productImageService.preSignedUrl(super.awsbucket, key))
                            .toList();

                    final var urls = CustomUtil.asynchronousTasks(req).join();

                    final var variants = CustomUtil.toVariantArray(pojo.variants(), EmployeeProductDetailService.class);

                    return DetailResponse.builder()
                            .isVisible(pojo.isVisible())
                            .colour(pojo.colour())
                            .urls(urls)
                            .variants(variants)
                            .build();
                })
                .toList();

        return CustomUtil.asynchronousTasks(futures).join();
    }

    /**
     * Create new {@link ProductDetail}.
     *
     * @param dto of type {@link ProductDetailDto}.
     * @throws CustomNotFoundException is thrown if product uuid does not exist.
     * @throws DuplicateException      is thrown if product colour exists.
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(final ProductDetailDto dto, final MultipartFile[] multipartFiles) {
        final var product = productRepository
                .productByUuid(dto.uuid())
                .orElseThrow(() -> new CustomNotFoundException("Product does not exist"));

        final Optional<ProductDetail> exist = detailRepo.productDetailByColour(dto.colour());

        if (exist.isPresent()) {
            skuService.save(dto.sizeInventory(), exist.get());
            return;
        }

        final var files = CustomUtil.transformMultipartFile.apply(multipartFiles, new StringBuilder());

        // save ProductDetail
        final var detail = detailRepo.save(ProductDetail.builder()
                .productId(product.productId())
                .colour(dto.colour())
                .isVisible(dto.visible())
                .build());

        // save ProductSku
        skuService.save(dto.sizeInventory(), detail);

        productImageService.saveProductImages(detail, files, super.awsbucket);
    }

    /**
     * Updates a {@link ProductDetail} based on {@link ProductSku} sku
     *
     * @param dto of type {@link UpdateProductDetailDto}.
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(final UpdateProductDetailDto dto) {
        detailRepo.updateProductSkuAndProductDetailByProductSku(
                dto.sku(),
                dto.colour(),
                dto.isVisible(),
                dto.qty(),
                dto.size()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public ProductDetail productDetail(
            final Product product,
            final String colour,
            final boolean visible
    ) {
        return detailRepo.save(ProductDetail.builder()
                .productId(product.productId())
                .colour(colour)
                .isVisible(visible)
                .build());
    }

}
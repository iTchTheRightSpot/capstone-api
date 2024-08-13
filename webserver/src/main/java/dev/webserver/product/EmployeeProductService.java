package dev.webserver.product;

import dev.webserver.AbstractEnvironment;
import dev.webserver.category.CategoryRepository;
import dev.webserver.enumeration.CapstoneCurrency;
import dev.webserver.exception.*;
import dev.webserver.util.CustomUtil;
import dev.webserver.util.Page;
import dev.webserver.util.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static dev.webserver.enumeration.CapstoneCurrency.NGN;
import static dev.webserver.enumeration.CapstoneCurrency.USD;
import static java.math.RoundingMode.FLOOR;

@Service
public class EmployeeProductService extends AbstractEnvironment {

    private static final Logger log = LoggerFactory.getLogger(EmployeeProductService.class);

    private final ProductPriceCurrencyRepository currencyRepo;
    private final ProductRepository productRepository;
    private final EmployeeProductDetailService detailService;
    private final ProductSkuService skuService;
    private final CategoryRepository categoryRepository;
    private final ProductImageService productImageService;

    protected EmployeeProductService(final Environment environment, final ProductPriceCurrencyRepository currencyRepo, final ProductRepository productRepository, final EmployeeProductDetailService detailService, final ProductSkuService skuService, final CategoryRepository categoryRepository, final ProductImageService productImageService) {
        super(environment);
        this.currencyRepo = currencyRepo;
        this.productRepository = productRepository;
        this.detailService = detailService;
        this.skuService = skuService;
        this.categoryRepository = categoryRepository;
        this.productImageService = productImageService;
    }

    public Pageable<ProductResponse> allProducts(
            final CapstoneCurrency currency, final int page, final int size
    ) {
        final Page of = Page.of(page, size);
        final int count = productRepository.countAllProductsForAdminFront();
        final var listOfProducts = productRepository.allProductsForAdminFront(of, currency);

        final var futures = listOfProducts.stream()
                .map(p -> (Supplier<ProductResponse>) () -> new ProductResponse(
                        p.uuid(),
                        p.name(),
                        p.description(),
                        p.price(),
                        p.currency().name(),
                        productImageService.preSignedUrl(awsbucket, p.imageKey()),
                        p.categoryName(),
                        p.weight(),
                        p.weightType()
                ))
                .toList();

        final var products = CustomUtil.asynchronousTasks(futures).join();
        return new Pageable<>(of, count, products);
    }

    /**
     * Create a new {@link Product}.
     *
     * @param multipartFiles of type {@link MultipartFile}.
     * @param dto   of type {@link CreateProductDto}.
     * @throws CustomNotFoundException is thrown if categoryId name does not exist in database.
     * or currency passed in truncateAmount does not contain in dto property priceCurrency.
     * @throws CustomServerError      is thrown if File is not an image.
     * @throws DuplicateException      is thrown if dto image exists in for Product.
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(final CreateProductDto dto, final MultipartFile[] multipartFiles) {
        if (!CustomUtil.validateContainsCurrencies(dto.priceCurrency())) {
            throw new CustomInvalidFormatException("please check currencies and prices");
        }

        final var category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new CustomNotFoundException("category not found"));

        // throw error if product exits
        if (productRepository.productByName(dto.name().trim()).isPresent()) {
            throw new DuplicateException(dto.name() + " exists");
        }

        final StringBuilder defaultImageKey = new StringBuilder();
        final var files = CustomUtil.transformMultipartFile.apply(multipartFiles, defaultImageKey);

        // save Product
        final var product = productRepository.save(
                Product.builder()
                        .categoryId(category.categoryId())
                        .uuid(UUID.randomUUID().toString())
                        .name(dto.name().trim())
                        .description(dto.desc().trim())
                        .defaultKey(defaultImageKey.toString())
                        .weight(dto.weight())
                        .weightType("kg")
                        .build());

        // save ngn & usd price
        final BigDecimal ngn = truncateAmount.apply(dto.priceCurrency(), NGN);
        final BigDecimal usd = truncateAmount.apply(dto.priceCurrency(), USD);
        currencyRepo.save(new ProductPriceCurrency(null, ngn, NGN, product.productId()));
        currencyRepo.save(new ProductPriceCurrency(null, usd, USD, product.productId()));

        // save ProductDetails
        final var detail = detailService.productDetail(product, dto.colour(), dto.visible());

        // save ProductSKUs
        skuService.save(dto.sizeInventory(), detail);

        // build and save ProductImages (save to s3)
        productImageService.saveProductImages(detail, files, awsbucket);
    }

    /**
     * Method updates a {@link Product} obj based on its UUID.
     *
     * @param dto of type {@link UpdateProductDto}.
     * @throws CustomNotFoundException when dto category_id or collection_id does not exist.
     * @throws DuplicateException      when new product name exist but not associated to product uuid.
     * @throws CustomInvalidFormatException if price is less than zero.
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(final UpdateProductDto dto) {
        if (dto.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomInvalidFormatException("price cannot be zero");
        }

        final var price = dto.price().setScale(2, RoundingMode.FLOOR);

        final boolean bool = productRepository
                .nameNotAssociatedToUuid(dto.uuid(), dto.name()) > 0;

        if (bool) {
            throw new DuplicateException(dto.name() + " exists");
        }

        final var category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new CustomNotFoundException("category not found"));

        productRepository.updateProduct(
                dto.uuid().trim(),
                dto.name().trim(),
                dto.desc().trim(),
                dto.weight(),
                category.categoryId()
        );

        // update price
        final var currency = CapstoneCurrency.valueOf(dto.currency().toUpperCase());
        currencyRepo.updateProductPriceByProductUuidAndCurrency(dto.uuid(), price, currency);
    }

    /**
     * Permanently deletes a {@link Product}.
     *
     * @param uuid is a unique string for every {@link Product}.
     * @throws ResourceAttachedException is thrown if Product has ProductDetails attached.
     * @throws CustomServerError               is thrown when an error occurs when deleting from s3.
     * @see <a href="https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/javav2/example_code/s3/src/main/java/com/example/s3/DeleteMultiObjects.java">documentation</a>
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(final String uuid) {
        final var product = productRepository.productByUuid(uuid)
                .orElseThrow(() -> new CustomNotFoundException("product not found"));

        try {
            productRepository.deleteByProductUuid(uuid);
        } catch (DataIntegrityViolationException e) {
            log.error("resources attached to Product {}", e.getMessage());
            throw new ResourceAttachedException("resource(s) attached to product");
        }

        final var keys = new ArrayList<>(
                productRepository.productImagesByProductUuid(uuid).stream()
                        .map(img -> ObjectIdentifier.builder().key(img.imageKey()).build())
                        .toList());
        keys.add(ObjectIdentifier.builder().key(product.defaultKey()).build());

        productImageService.deleteFromS3(keys, awsbucket);
    }

    /**
     * Retrieves the price based on the currency.
     * */
    final BiFunction<PriceCurrencyDto[], CapstoneCurrency, BigDecimal> truncateAmount = (arr, curr) -> Arrays
            .stream(arr)
            .filter(priceCurrencyDTO -> priceCurrencyDTO.currency().equals(curr.name()))
            .map(obj -> obj.price().setScale(2, FLOOR))
            .findFirst()
            .orElseThrow(() -> new CustomNotFoundException("please enter %s amount".formatted(curr.name())));
}
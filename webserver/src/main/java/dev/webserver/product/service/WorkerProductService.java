package dev.webserver.product.service;

import dev.webserver.category.service.WorkerCategoryService;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.exception.*;
import dev.webserver.product.dto.CreateProductDTO;
import dev.webserver.product.dto.PriceCurrencyDto;
import dev.webserver.product.dto.UpdateProductDTO;
import dev.webserver.product.entity.PriceCurrency;
import dev.webserver.product.entity.Product;
import dev.webserver.product.projection.ProductPojo;
import dev.webserver.product.repository.PriceCurrencyRepo;
import dev.webserver.product.repository.ProductRepo;
import dev.webserver.product.response.CustomMultiPart;
import dev.webserver.product.response.ProductResponse;
import dev.webserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static dev.webserver.enumeration.SarreCurrency.NGN;
import static dev.webserver.enumeration.SarreCurrency.USD;
import static java.math.RoundingMode.FLOOR;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class WorkerProductService {

    private static final Logger log = LoggerFactory.getLogger(WorkerProductService.class);

    @Value(value = "${aws.bucket}")
    @Setter
    private String BUCKET;

    private final PriceCurrencyRepo currencyRepo;
    private final ProductRepo productRepo;
    private final WorkerProductDetailService detailService;
    private final ProductSkuService skuService;
    private final WorkerCategoryService categoryService;
    private final HelperService helperService;

    /**
     * Sample issue
     * <p>
     * We have a {@link org.springframework.data.domain.Page} of {@link ProductPojo}
     * we need to map to a {@link ProductResponse}. The caveat is whilst mapping
     * to a {@link ProductResponse}, we need to make a call to S3 to
     * retrieve a pre-signed url for each {@link ProductResponse} object.
     * Because we want to achieve this concurrently to improve performance
     * we need to make use of java multithreading feature. We are going
     * to use java 21 VirtualThread feature to take advantage of high
     * through put and to rely on the jvm to manage thread creation instead of
     * us creating Platform threads.
     * <p>
     * 1. Retrieve the {@link Page} of {@link ProductPojo} from db.
     * <p>
     * 2. Instantiate a VirtualThread executor.
     * <p>
     * 3. Iterate the contents of {@link Page} whilst assigning tasks to
     * executor/thread.
     * <p>
     * 4. Do not block each thread by manually calling join instead we wait
     * for all completion before calling join.
     * <p>
     * 5. We wait for all executions to complete by using
     * {@link CompletableFuture} property allOf.
     * <p>
     * 6. Return the response to the UI.
     *
     * @param currency The users choice of currency to return for each {@link Product}.
     * @param page     the page used of construct a {@link PageRequest}.
     * @param size     the size used of construct a {@link PageRequest}.
     * @return a {@link CompletableFuture} of {@link Page} of {@link ProductResponse}.
     */
    public CompletableFuture<Page<ProductResponse>> allProducts(
            final SarreCurrency currency, final int page, final int size
    ) {
        var pageOfProducts = this.productRepo
                .allProductsForAdminFront(currency, PageRequest.of(page, size));

        var futures = createTasks(pageOfProducts);

        return CustomUtil.asynchronousTasks(futures, WorkerProductService.class)
                .thenApply(v -> new PageImpl<>(
                        v.stream().map(Supplier::get).toList(),
                        pageOfProducts.getPageable(),
                        pageOfProducts.getTotalElements()
                ));
    }

    /**
     * Creates a list of Suppliers representing tasks to asynchronously generate
     * {@link ProductResponse} objects. Each Supplier encapsulates the creation
     * logic for a single {@link ProductResponse} object.
     *
     * @param page The page of {@link ProductPojo} objects from which to generate
     *              {@link ProductResponse} objects.
     * @return A list of {@link Supplier}, each representing a task to create a
     * {@link ProductResponse} object.
     */
    private List<Supplier<ProductResponse>> createTasks(final Page<ProductPojo> page) {
        return page.stream()
                .map(p -> (Supplier<ProductResponse>) () -> new ProductResponse(
                        p.getUuid(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getCurrency(),
                        helperService.preSignedUrl(BUCKET, p.getImage()),
                        p.getCategory(),
                        p.getWeight(),
                        p.getWeightType()
                ))
                .toList();
    }

    /**
     * Create a new {@link Product}.
     *
     * @param multipartFiles of type {@link MultipartFile}.
     * @param dto   of type {@link CreateProductDTO}.
     * @throws CustomNotFoundException is thrown if categoryId name does not exist in database.
     * or currency passed in truncateAmount does not contain in dto property priceCurrency.
     * @throws CustomServerError      is thrown if File is not an image.
     * @throws DuplicateException      is thrown if dto image exists in for Product.
     */
    public void create(final CreateProductDTO dto, final MultipartFile[] multipartFiles) {
        if (!CustomUtil.validateContainsCurrencies(dto.priceCurrency())) {
            throw new CustomInvalidFormatException("please check currencies and prices");
        }

        var category = this.categoryService.findById(dto.categoryId());

        // throw error if product exits
        if (this.productRepo.productByName(dto.name().trim()).isPresent()) {
            throw new DuplicateException(dto.name() + " exists");
        }

        StringBuilder defaultImageKey = new StringBuilder();
        var files = CustomUtil.transformMultipartFile.apply(multipartFiles, defaultImageKey);

        // build Product
        var p = Product.builder()
                .productCategory(category)
                .uuid(UUID.randomUUID().toString())
                .name(dto.name().trim())
                .description(dto.desc().trim())
                .defaultKey(defaultImageKey.toString())
                .weight(dto.weight())
                .weightType("kg")
                .productDetails(new HashSet<>())
                .build();

        // save Product
        var product = this.productRepo.save(p);

        // save ngn & usd price
        BigDecimal ngn = truncateAmount.apply(dto.priceCurrency(), NGN);
        BigDecimal usd = truncateAmount.apply(dto.priceCurrency(), USD);
        this.currencyRepo.save(new PriceCurrency(ngn, NGN, product));
        this.currencyRepo.save(new PriceCurrency(usd, USD, product));

        // save ProductDetails
        var date = CustomUtil.toUTC(new Date());
        var detail = this.detailService.
                productDetail(product, dto.colour(), dto.visible(), date);

        // save ProductSKUs
        this.skuService.save(dto.sizeInventory(), detail);

        // build and save ProductImages (save to s3)
        this.helperService.saveProductImages(detail, files, BUCKET);
    }

    /**
     * Method updates a {@link Product} obj based on its UUID.
     *
     * @param dto of type {@link UpdateProductDTO}.
     * @throws CustomNotFoundException when dto category_id or collection_id does not exist.
     * @throws DuplicateException      when new product name exist but not associated to product uuid.
     * @throws CustomInvalidFormatException if price is less than zero.
     */
    public void update(final UpdateProductDTO dto) {
        if (dto.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomInvalidFormatException("price cannot be zero");
        }

        var price = dto.price().setScale(2, RoundingMode.FLOOR);

        boolean bool = this.productRepo
                .nameNotAssociatedToUuid(dto.uuid(), dto.name()) > 0;

        if (bool) {
            throw new DuplicateException(dto.name() + " exists");
        }

        var category = this.categoryService.findById(dto.categoryId());

        this.productRepo
                .updateProduct(
                        dto.uuid().trim(),
                        dto.name().trim(),
                        dto.desc().trim(),
                        dto.weight(),
                        category
                );

        // update price
        var currency = SarreCurrency.valueOf(dto.currency().toUpperCase());
        this.currencyRepo
                .updateProductPriceByProductUuidAndCurrency(dto.uuid(), price, currency);
    }

    /**
     * Permanently deletes a {@link Product}.
     *
     * @param uuid is a unique string for every {@link Product}.
     * @throws ResourceAttachedException is thrown if Product has ProductDetails attached.
     * @throws CustomServerError               is thrown when an error occurs when deleting from s3.
     * @see <a href="https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/javav2/example_code/s3/src/main/java/com/example/s3/DeleteMultiObjects.java">documentation</a>
     */
    public void delete(final String uuid) {
        final List<ObjectIdentifier> keys = this.productRepo.productImagesByProductUuid(uuid)
                .stream() //
                .map(img -> ObjectIdentifier.builder().key(img.getImage()).build()) //
                .toList();

        try {
            this.productRepo.deleteByProductUuid(uuid);
        } catch (DataIntegrityViolationException e) {
            log.error("resources attached to Product {}", e.getMessage());
            throw new ResourceAttachedException("resource(s) attached to product");
        }

        if (!keys.isEmpty()) {
            this.helperService.deleteFromS3(keys, BUCKET);
        }
    }

    /**
     * Retrieves the price based on the currency.
     * */
    final BiFunction<PriceCurrencyDto[], SarreCurrency, BigDecimal> truncateAmount = (arr, curr) -> Arrays
            .stream(arr)
            .filter(priceCurrencyDTO -> priceCurrencyDTO.currency().equals(curr.name()))
            .map(obj -> obj.price().setScale(2, FLOOR))
            .findFirst()
            .orElseThrow(() -> new CustomNotFoundException("please enter %s amount".formatted(curr.name())));
}
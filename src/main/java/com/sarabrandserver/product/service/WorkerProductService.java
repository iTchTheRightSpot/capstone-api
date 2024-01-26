package com.sarabrandserver.product.service;

import com.sarabrandserver.category.service.WorkerCategoryService;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.*;
import com.sarabrandserver.product.dto.CreateProductDTO;
import com.sarabrandserver.product.dto.PriceCurrencyDTO;
import com.sarabrandserver.product.dto.UpdateProductDTO;
import com.sarabrandserver.product.entity.PriceCurrency;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.repository.PriceCurrencyRepo;
import com.sarabrandserver.product.repository.ProductRepo;
import com.sarabrandserver.product.response.ProductResponse;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.BiFunction;

import static com.sarabrandserver.enumeration.SarreCurrency.NGN;
import static com.sarabrandserver.enumeration.SarreCurrency.USD;
import static java.math.RoundingMode.FLOOR;

@Service
@RequiredArgsConstructor
@Setter
public class WorkerProductService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private final PriceCurrencyRepo currencyRepo;
    private final ProductRepo productRepo;
    private final WorkerProductDetailService detailService;
    private final ProductSKUService skuService;
    private final WorkerCategoryService categoryService;
    private final HelperService helperService;

    /**
     * Method returns a list of ProductResponse. Note fetchAllProductsWorker query method returns a list of
     * ProductPojo using spring jpa projection. It only returns a Product not Including its details.
     *
     * @param currency is of SarreCurrency
     * @param page is the UI page number
     * @param size is the max amount to be displayed on a page
     * @return Page of ProductResponse
     */
    public Page<ProductResponse> allProducts(SarreCurrency currency, int page, int size) {
        return this.productRepo
                .allProductsAdminFront(currency, PageRequest.of(page, size))
                .map(pojo -> {
                    var url = this.helperService.preSignedURL(BUCKET, pojo.getImage());
                    return ProductResponse.builder()
                            .category(pojo.getCategory())
                            .id(pojo.getUuid())
                            .name(pojo.getName())
                            .desc(pojo.getDescription())
                            .price(pojo.getPrice())
                            .currency(pojo.getCurrency())
                            .imageUrl(url)
                            .weight(pojo.getWeight())
                            .weightType(pojo.getWeightType())
                            .build();
                });
    }

    /**
     * Create a new Product.
     *
     * @param files of type MultipartFile
     * @param dto   of type CreateProductDTO
     * @throws CustomNotFoundException is thrown if categoryId name does not exist in database
     * or currency passed in truncateAmount does not contain in dto property priceCurrency
     * @throws CustomAwsException      is thrown if File is not an image
     * @throws DuplicateException      is thrown if dto image exists in for Product
     */
    @Transactional
    public void create(final CreateProductDTO dto, final MultipartFile[] files) {
        if (!CustomUtil.validateContainsCurrencies(dto.priceCurrency())) {
            throw new CustomInvalidFormatException("please check currencies and prices");
        }

        var category = this.categoryService.findById(dto.categoryId());

        // throw error if product exits
        if (this.productRepo.productByName(dto.name().trim()).isPresent()) {
            throw new DuplicateException(dto.name() + " exists");
        }

        // validate MultipartFile[] are all images
        StringBuilder defaultImageKey = new StringBuilder();
        var file = this.helperService.customMultiPartFiles(files, defaultImageKey);

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
        this.helperService.productImages(detail, file, BUCKET);
    }

    /**
     * Method updates a Product obj based on its UUID.
     *
     * @param dto of type UpdateProductDTO
     * @throws CustomNotFoundException when dto category_id or collection_id does not exist
     * @throws DuplicateException      when new product name exist but not associated to product uuid
     * @throws CustomInvalidFormatException if price is less than zero
     */
    @Transactional
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
                        category,
                        dto.weight()
                );

        // update price
        var currency = SarreCurrency.valueOf(dto.currency().toUpperCase());
        this.currencyRepo.updatePriceByProductUUID(dto.uuid(), price, currency);
    }

    // TODO validate product isn't in user session and it is not in order detail
    /**
     * Permanently deletes a Product db.
     *
     * @param uuid is the product uuid
     * @throws CustomNotFoundException   is thrown when Product id does not exist
     * @throws ResourceAttachedException is thrown if Product has ProductDetails attached
     * @throws S3Exception               is thrown when deleting from s3
     */
    @Transactional
    public void delete(final String uuid) {
        var product = this.productRepo.productByUuid(uuid)
                .orElseThrow(() -> new CustomNotFoundException(uuid + " does not exist"));

        if (this.productRepo.productDetailAttach(uuid) > 1 || this.skuService.itemBeenBought(uuid) > 0) {
            throw new ResourceAttachedException(
                    "cannot delete %s as it has many variants".formatted(product.getName())
            );
        }

        List<ObjectIdentifier> keys = this.productRepo.productImagesByProductUUID(uuid)
                .stream() //
                .map(img -> ObjectIdentifier.builder().key(img.getImage()).build()) //
                .toList();

        // delete from S3 only if keys contain
        if (!keys.isEmpty()) {
            this.helperService.deleteFromS3(keys, BUCKET);
        }

        // delete from Database
        this.productRepo.delete(product);
    }

    /**
     * Retrieves the price based on the currency.
     * */
    final BiFunction<PriceCurrencyDTO[], SarreCurrency, BigDecimal> truncateAmount = (arr, curr) -> Arrays
                    .stream(arr)
                    .filter(priceCurrencyDTO -> priceCurrencyDTO.currency().equals(curr.name()))
                    .map(obj -> obj.price().setScale(2, FLOOR))
                    .findFirst()
                    .orElseThrow(() -> new CustomNotFoundException("please enter %s amount".formatted(curr.name())));
}
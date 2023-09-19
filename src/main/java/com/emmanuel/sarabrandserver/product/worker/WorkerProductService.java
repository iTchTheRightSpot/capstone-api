package com.emmanuel.sarabrandserver.product.worker;

import com.emmanuel.sarabrandserver.aws.S3Service;
import com.emmanuel.sarabrandserver.category.service.WorkerCategoryService;
import com.emmanuel.sarabrandserver.collection.service.WorkerCollectionService;
import com.emmanuel.sarabrandserver.exception.CustomAwsException;
import com.emmanuel.sarabrandserver.exception.CustomNotFoundException;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.exception.ResourceAttachedException;
import com.emmanuel.sarabrandserver.product.entity.Product;
import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import com.emmanuel.sarabrandserver.product.entity.ProductImage;
import com.emmanuel.sarabrandserver.product.entity.ProductSku;
import com.emmanuel.sarabrandserver.product.repository.ProductDetailRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductImageRepo;
import com.emmanuel.sarabrandserver.product.repository.ProductRepository;
import com.emmanuel.sarabrandserver.product.repository.ProductSkuRepo;
import com.emmanuel.sarabrandserver.product.util.*;
import com.emmanuel.sarabrandserver.util.CustomUtil;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;

@Service @Setter
public class WorkerProductService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    @Value(value = "${spring.profiles.active}")
    private String ACTIVEPROFILE;

    private static final Logger log = Logger.getLogger(WorkerProductService.class.getName());

    private final ProductRepository productRepository;
    private final ProductDetailRepo detailRepo;
    private final ProductImageRepo productImageRepo;
    private final ProductSkuRepo productSkuRepo;
    private final WorkerCategoryService categoryService;
    private final CustomUtil customUtil;
    private final WorkerCollectionService collectionService;
    private final S3Service s3Service;

    public WorkerProductService(
            ProductRepository productRepository,
            ProductDetailRepo detailRepo,
            ProductImageRepo productImageRepo,
            ProductSkuRepo productSkuRepo,
            WorkerCategoryService categoryService,
            CustomUtil customUtil,
            WorkerCollectionService collectionService,
            S3Service s3Service
    ) {
        this.productRepository = productRepository;
        this.detailRepo = detailRepo;
        this.productImageRepo = productImageRepo;
        this.productSkuRepo = productSkuRepo;
        this.categoryService = categoryService;
        this.customUtil = customUtil;
        this.collectionService = collectionService;
        this.s3Service = s3Service;
    }

    /**
     * Method fetches a list of ProductResponse. Note fetchAllProductsWorker query method returns a list of
     * ProductPojo using spring jpa projection. It only returns a Product not Including its details.
     *
     * @param page is the UI page number
     * @param size is the max amount to be displayed on a page
     * @return Page of ProductResponse
     */
    public Page<ProductResponse> fetchAll(int page, int size) {
        boolean bool = this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage");

        return this.productRepository
                .fetchAllProductsWorker(PageRequest.of(page, size))
                .map(pojo -> {
                    var url = this.s3Service.getPreSignedUrl(bool, BUCKET, pojo.getKey());
                    return ProductResponse.builder()
                            .category(pojo.getCategory())
                            .collection(pojo.getCollection())
                            .id(pojo.getUuid())
                            .name(pojo.getName())
                            .desc(pojo.getDesc())
                            .price(pojo.getPrice())
                            .currency(pojo.getCurrency())
                            .imageUrl(url)
                            .build();
                });
    }

    /**
     * Returns a List of ProductDetail based on Product uuid
     *
     * @param uuid is the uuid of the product
     * @return List of DetailResponse
     */
    public List<DetailResponse> productDetailsByProductUUID(String uuid) {
        boolean bool = this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage");

        return this.detailRepo
                .findProductDetailsByProductUuidWorker(uuid) //
                .stream() //
                .map(pojo -> {
                    var urls = Arrays.stream(pojo.getImage().split(","))
                            .map(key -> this.s3Service.getPreSignedUrl(bool, BUCKET, key))
                            .toList();

                    Variant[] variants = this.customUtil
                            .toVariantArray(pojo.getVariants(), WorkerProductService.class);

                    return DetailResponse.builder()
                            .isVisible(pojo.getVisible())
                            .colour(pojo.getColour())
                            .url(urls)
                            .variants(variants)
                            .build();
                })
                .toList();
    }

    /**
     * Create new ProductDetail
     *
     * @param dto of type DetailDTO
     * @throws CustomNotFoundException is thrown if product uuid does not exist
     * @throws DuplicateException is thrown if product colour exists
     * */
    @Transactional
    public void createDetail(ProductDetailDTO dto) {
        var product = this.productRepository
                .findByProductUuid(dto.getUuid().trim())
                .orElseThrow(() -> new CustomNotFoundException("Product does not exist"));

        // TODO validate colour does not exist

        var date = this.customUtil.toUTC(new Date()).orElse(new Date());

        // Validate MultipartFile[] are all images
        CustomMultiPart[] file = validateMultiPartFile(dto.getFiles(), new StringBuilder());

        // Save ProductDetail
        var detail = productDetail(product, dto.getColour(), dto.getVisible(), date);

        // Save ProductSKUs
        saveProductSKUs(dto.getSizeInventory(), detail);

        // Save ProductImages (save to s3)
        productImages(detail, file, this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage"));
    }


    /**
     * Create a new Product.
     *
     * @param files of type MultipartFile
     * @param dto   of type CreateProductDTO
     * @throws CustomNotFoundException is thrown when category or collection name does not exist
     * @throws CustomAwsException      is thrown if File is not an image
     * @throws DuplicateException      is thrown if dto image exists in for Product
     */
    @Transactional
    public void create(CreateProductDTO dto, MultipartFile[] files) {
        var category = this.categoryService.findByName(dto.getCategory().trim());
        var _product = this.productRepository.findByProductName(dto.getName().trim());
        var date = this.customUtil.toUTC(new Date()).orElse(new Date());
        boolean bool = this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage");

        // throw error if product exits
        if (_product.isPresent()) {
	        throw new DuplicateException(dto.getName() + " exists");
        }

        StringBuilder defaultImageKey = new StringBuilder();
        // Validate MultipartFile[] are all images
        CustomMultiPart[] multiPartFile = validateMultiPartFile(files, defaultImageKey);

        // Build Product
        var product = Product.builder()
                .productCategory(category)
                .uuid(UUID.randomUUID().toString())
                .name(dto.getName().trim())
                .description(dto.getDesc().trim())
                .defaultKey(defaultImageKey.toString())
                .price(dto.getPrice())
                .currency(dto.getCurrency()) // default is USD
                .productDetails(new HashSet<>())
                .build();

        // Set ProductCollection to Product
        if (!dto.getCollection().isBlank()) {
            var collection = this.collectionService.findByName(dto.getCollection().trim());
            product.setProductCollection(collection);
        }

        // Save Product
        var saved = this.productRepository.save(product);

        // Save ProductDetails
        var detail = productDetail(saved, dto.getColour(), dto.getVisible(), date);

        // Save ProductSKUs
        saveProductSKUs(dto.getSizeInventory(), detail);

        // Build ProductImages (save to s3)
        productImages(detail, multiPartFile, bool);
    }

    /**
     * Save Product sku. Look in db diagram in read me in case of confusion
     */
    private void saveProductSKUs(SizeInventoryDTO[] dto, ProductDetail detail) {
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
     * Save ProductDetail
     */
    private ProductDetail productDetail(Product product, String colour, boolean visible, Date date) {
        var detail = ProductDetail.builder()
                .product(product)
                .colour(colour)
                .createAt(date)
                .isVisible(visible)
                .productImages(new HashSet<>())
                .skus(new HashSet<>())
                .build();

        // Save ProductDetail
        return this.detailRepo.save(detail);
    }

    /**
     * Save to s3 before Create ProductImage
     * @throws CustomAwsException if there is an error uploading to S3
     */
    private void productImages(ProductDetail detail, CustomMultiPart[] files, boolean profile) {
        for (CustomMultiPart file : files) {
            var obj = new ProductImage(file.key(), file.file().getAbsolutePath(), detail);

            // Upload image to S3 if in desired profile
            if (profile) {
                this.s3Service.uploadToS3(file.file(), file.metadata(), BUCKET, file.key());
            }

            // Save ProductImage
            this.productImageRepo.save(obj);
        }
    }

    /**
     * Method updates a Product obj based on its UUID.
     *
     * @param dto of type UpdateProductDTO
     * @throws CustomNotFoundException when dto category_id or collection_id does not exist
     * @throws DuplicateException      when new product name exist but not associated to product uuid
     */
    @Transactional
    public void updateProduct(final UpdateProductDTO dto) {
        boolean bool = this.productRepository
                .nameNotAssociatedToUuid(dto.getUuid(), dto.getName()) > 0;

        if (bool) {
            throw new DuplicateException(dto.getName() + " exists");
        }

        var category = this.categoryService.findByUuid(dto.getCategoryId());

        if (!dto.getCollection().isEmpty()) {
            // Find ProductCollection by uuid
            var collection = this.collectionService.findByUuid(dto.getCollectionId());

            this.productRepository.updateProductCategoryCollectionPresent(
                    dto.getUuid(),
                    dto.getName().trim(),
                    dto.getDesc().trim(),
                    dto.getPrice(),
                    category,
                    collection
            );

            return;
        }

        this.productRepository.updateProductCollectionNotPresent(
                dto.getUuid(),
                dto.getName().trim(),
                dto.getDesc().trim(),
                dto.getPrice(),
                category
        );
    }

    /**
     * Updates a ProductDetail and its relationship with other tables except ProductImage
     *
     * @param dto of type DetailDTO
     */
    @Transactional
    public void updateProductDetail(final UpdateProductDetailDTO dto) {
        this.detailRepo.updateProductDetail(
                dto.getSku(),
                dto.getIsVisible(),
                dto.getQty(),
                dto.getSize()
        );
    }

    /**
     * Method permanently deletes a Product and children from db.
     * Note: A product can only be deleted if it has less than 1 ProductDetail attached.
     *
     * @param uuid is the product uuid
     * @throws CustomNotFoundException is thrown when Product id does not exist
     * @throws ResourceAttachedException is thrown if Product has ProductDetails attached
     * @throws S3Exception             is thrown when deleting from s3
     */
    @Transactional
    public void deleteProduct(final String uuid) {
        var product = this.productRepository.findByProductUuid(uuid)
                .orElseThrow(() -> new CustomNotFoundException(uuid + " does not exist"));

        boolean bool = this.productRepository.productDetailAttach(uuid) > 1;

        if (bool) {
            throw new ResourceAttachedException("%s has product variants".formatted(product.getName()));
        }

        // Delete from S3
        if (this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage")) {
            // Get all Images
            List<ObjectIdentifier> keys = this.productRepository.images(uuid)
                    .stream() //
                    .map(img -> ObjectIdentifier.builder().key(img.getImage()).build()) //
                    .toList();

            if (!keys.isEmpty()) {
                this.s3Service.deleteFromS3(keys, BUCKET);
            }
        }

        // Delete from Database
        this.productRepository.delete(product);
    }

    /**
     * Method permanently deletes a ProductDetail there by deleting relationship with Product.
     * Note ProductDetail has an EAGER fetch time with ProductImage
     *
     * @param sku is a unique String for each ProductDetail
     * @throws CustomNotFoundException is thrown when sku does not exist
     * @throws S3Exception             is thrown when deleting from s3
     */
    @Transactional
    public void deleteProductDetail(final String sku) {
        var detail = findByDetailBySku(sku);

        if (this.ACTIVEPROFILE.equals("prod") || this.ACTIVEPROFILE.equals("stage")) {
            List<ObjectIdentifier> keys = detail.getProductImages() //
                    .stream() //
                    .map(image -> ObjectIdentifier.builder().key(image.getImageKey()).build())
                    .toList();

            if (!keys.isEmpty()) {
                this.s3Service.deleteFromS3(keys, BUCKET);
            }
        }

        // Remove detail from Product and Save Product
        this.detailRepo.delete(detail);
    }

    // Find ProductDetail by sku
    private ProductDetail findByDetailBySku(String sku) {
        return this.productRepository.findDetailBySku(sku)
                .orElseThrow(() -> new CustomNotFoundException("SKU does not exist"));
    }

    /**
     * Validates if items in MultipartFile array are all images, else an error is thrown.
     * Note I am returning an array as it is a bit more efficient than arraylist in terms of memory
     *
     * @param multipartFiles is an array of MultipartFile
     * @return CustomMultiPart array
     */
    private CustomMultiPart[] validateMultiPartFile(MultipartFile[] multipartFiles, StringBuilder defaultKey) {
        CustomMultiPart[] arr = new CustomMultiPart[multipartFiles.length];

        for (int i = 0; i < multipartFiles.length; i++) {
            String originalFileName = Objects.requireNonNull(multipartFiles[i].getOriginalFilename());

            File file = new File(originalFileName);

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                // write MultipartFile to file
                outputStream.write(multipartFiles[i].getBytes());

                // Validate file is an image
                String contentType = Files.probeContentType(file.toPath());
                if (!contentType.startsWith("image/")) {
                    log.warning("File is not an image");
                    throw new CustomAwsException("File is not an image");
                }

                // Create image metadata for storing in AWS
                Map<String, String> metadata = new HashMap<>();
                metadata.put("Content-Type", contentType);
                metadata.put("Title", originalFileName);
                metadata.put("Type", StringUtils.getFilenameExtension(originalFileName));

                // Default key
                String key = UUID.randomUUID().toString();
                if (defaultKey.isEmpty()) {
                    defaultKey.append(key);
                }

                // Copy into array
                arr[i] = new CustomMultiPart(file, metadata, key);
            } catch (IOException ex) {
                log.warning("Error either writing multipart to file or getting file type. {}" + ex.getMessage());
                throw new CustomAwsException("Please verify file is an image");
            }
        }
        return arr;
    }

}

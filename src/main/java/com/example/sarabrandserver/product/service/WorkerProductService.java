package com.example.sarabrandserver.product.service;

import com.example.sarabrandserver.category.service.CategoryService;
import com.example.sarabrandserver.exception.CustomNotFoundException;
import com.example.sarabrandserver.product.dto.CreateProductDTO;
import com.example.sarabrandserver.product.dto.UpdateProductDTO;
import com.example.sarabrandserver.product.entity.*;
import com.example.sarabrandserver.product.repository.ProductDetailRepo;
import com.example.sarabrandserver.product.repository.ProductRepository;
import com.example.sarabrandserver.product.response.ImageResponse;
import com.example.sarabrandserver.product.response.WorkerProductResponse;
import com.example.sarabrandserver.util.DateUTC;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.springframework.http.HttpStatus.*;

@Service
public class WorkerProductService {
    private final ProductRepository productRepository;
    private final ProductDetailRepo productDetailRepo;
    private final CategoryService categoryService;
    private final DateUTC dateUTC;

    public WorkerProductService(
            ProductRepository productRepository,
            ProductDetailRepo productDetailRepo,
            CategoryService categoryService,
            DateUTC dateUTC
    ) {
        this.productRepository = productRepository;
        this.productDetailRepo = productDetailRepo;
        this.categoryService = categoryService;
        this.dateUTC = dateUTC;
    }

    /**
     * Fetches all products and product details with pagination in mind. Also, a set max of 25 for size is set so not
     * to run into MaxHeap exception.
     * @param page is the UI page number
     * @param size is the max amount to be displayed on a page
     * */
    public List<?> fetchAll(int page, int size) {
        return this.productRepository
                .findAll(PageRequest.of(page, Math.min(size, 25)))
                .stream() //
                .map(product -> {
                    var res = new WorkerProductResponse(product);
                    return WorkerProductResponse.builder()
                            .name(res.getName())
                            .sku(res.getSku())
                            .price(res.getPrice())
                            .qty(res.getQty())
                            .createdAt(res.getCreatedAt())
                            .modifiedAt(res.getModifiedAt())
                            .deletedAt(res.getDeletedAt())
                            .productSize(res.getProductSize())
                            .productColour(res.getProductColour())
                            .imageResponses(res.imageResponses())
                            .build();
                }) //
                .toList();
    }

    /**
     * Method is responsible for creating and saving a Product.
     * 1. Find ProductCategory since it has a 1 to many relationship with Product
     * 2. Create a ProductDetail
     * 3. Verify Product name does not exist. If it does, add new ProductDetail to Product
     * 4. Create a new Product and Save
     * 5. Save to AWS or Digital Oceans S3 Bucket
     * 6. Save Product to ProductCategory
     * @param file of type MultipartFile
     * @param dto of type CreateProductDTO
     * @throws CustomNotFoundException is thrown when category name does not exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> create(CreateProductDTO dto, MultipartFile file) {
        // Step 1
        var category = this.categoryService.findByName(dto.getCategoryName().trim());
        var date = this.dateUTC.toUTC(new Date()).isPresent() ? this.dateUTC.toUTC(new Date()).get() : new Date();

        // Step 2
        var detail = ProductDetail.builder()
                .createAt(date)
                .productImages(new HashSet<>())
                .productSizes(new HashSet<>())
                .productColours(new HashSet<>())
                .build();

        if (dto.getDetailDTO().getIsVisible()) {
            detail.setModifiedAt(date);
            detail.setDeletedAt(date);
        }

        // Step 3
        var findProduct = this.productRepository.findByProductName(dto.getProductName().trim());
        if (findProduct.isPresent()) {
            saveProduct(findProduct.get(), detail, dto, file);
            return new ResponseEntity<>("Added", OK);
        }

        // Step 4 and 5
        var product = Product.builder()
                .name(dto.getProductName().trim())
                .defaultImagePath(Objects.requireNonNull(file.getOriginalFilename()).trim())
                .build();
        if (dto.getIsVisible()) {
            product.setDeletedAt(date);
        }
        saveProduct(product, detail, dto, file);

        // Step 6
        category.addProduct(product);
        this.categoryService.save(category);

        return new ResponseEntity<>("Created", CREATED);
    }

    // TODO Product image to either Digital or AWS BUCKET
    private void saveProduct(
            final Product product,
            final ProductDetail detail,
            final CreateProductDTO dto,
            final MultipartFile file
    ) {
        // Create new Product Detail
        detail.setDescription(dto.getDetailDTO().getDesc().trim());
        detail.setSku(UUID.randomUUID().toString());
        detail.setQuantity(dto.getDetailDTO().getQty());
        detail.setPrice(dto.getDetailDTO().getPrice());
        detail.setCurrency(dto.getDetailDTO().getCurrency());

        // add image object
        var image = ProductImage.builder()
                .imageKey(UUID.randomUUID().toString())
                .imagePath(Objects.requireNonNull(file.getOriginalFilename()).trim())
                .build();
        detail.addImage(image);
        // add size
        detail.addSize(new ProductSize(dto.getDetailDTO().getSize()));
        // add colour
        detail.addColour(new ProductColour(dto.getDetailDTO().getColour()));
        // save and add ProductDetail to Product
        product.addProductDetail(this.productDetailRepo.save(detail));

        this.productRepository.save(product);
    }

    /**
     * Method is responsible for updating a Product and or ProductDetail. All properties of UpdateProductDTO
     * are none null and none empty (newName can have a length of zero but not null).
     * @param dto of type UpdateProductDTO
     * @param file of type MultipartFile
     * @throws CustomNotFoundException is thrown when Product name or sku does not exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> updateProduct(final UpdateProductDTO dto, final  MultipartFile file) {
        var product = findProductByName(dto.getOldName().trim());
        var detail = findByProductIDAndSku(product.getProductId(), dto.getSku().trim());

        // Update Product name
        if (!dto.getNewName().trim().equals(dto.getOldName().trim())) {
            this.productRepository.updateName(dto.getNewName().trim(), product.getProductId());
        }

        // Update Product Detail image
        if (file != null) {
            // TODO Update Image in S3 bucket before updating in DB
            this.productDetailRepo
                    .updateImage(product.getProductId(), Objects.requireNonNull(file.getOriginalFilename()).trim());
        }

        // Update none one to many properties of ProductDetail
        var date = this.dateUTC.toUTC(new Date()).isPresent() ? this.dateUTC.toUTC(new Date()).get() : new Date();
        this.productDetailRepo.updateProductDetail(
                detail.getProductDetailId(),
                dto.getDesc().trim(),
                dto.getPrice(),
                dto.getQty(),
                date
        );

        // Update ProductDetail size and colour
        this.productDetailRepo.updateProductDetailSize(detail.getProductDetailId(), dto.getSize());
        this.productDetailRepo.updateProductDetailColour(detail.getProductDetailId(), dto.getColour().trim());

        return new ResponseEntity<>("Updated", OK);
    }

    /**
     * Method permanently deletes a product and its child
     * @param name is the Product name
     * @throws CustomNotFoundException is thrown when Product name does not exist
     * @return ResponseEntity of type HttpStatus (204).
     * */
    @Transactional
    public ResponseEntity<?> deleteProduct(final String name) {
        var product = findProductByName(name);
        this.productRepository.delete(product);
        return new ResponseEntity<>(NO_CONTENT);
    }

    /**
     * Method permanently deletes a ProductDetail
     * @param name is the Product name
     * @param sku is a unique String for each ProductDetail
     * @throws CustomNotFoundException is thrown when Product name or SKU does not exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> deleteProductDetail(final String name, final String sku) {
        var product = findProductByName(name);
        var detail = findByProductIDAndSku(product.getProductId(), sku);
        this.productDetailRepo.delete(detail);
        return new ResponseEntity<>("Deleted", OK);
    }

    private Product findProductByName(String name) {
        return this.productRepository.findByProductName(name)
                .orElseThrow(() -> new CustomNotFoundException(name + " does not exist"));
    }

    private ProductDetail findByProductIDAndSku(long id, String sku) {
        return this.productDetailRepo
                .findByProductDetail(id, sku)
                .orElseThrow(() -> new CustomNotFoundException("SKU does not exist"));
    }

//    @NotNull
//    private ImageResponse s3_object_request(Product product, String key) throws IOException {
//        ImageResponse imageResponse;
//        GetObjectRequest request = GetObjectRequest.builder()
//                .key(key)
//                .bucket(BUCKET_NAME)
//                .build();
//
//        ResponseBytes<GetObjectResponse> bytes = this.s3Client.getObjectAsBytes(request);
//
//        imageResponse = new ImageResponse(
//                this.fileService.getImageName(product.getPath()),
//                fileService.getMediaType(product.getPath()),
//                bytes.asByteArray()
//        );
//        return imageResponse;
//    }

}

package com.example.sarabrandserver.product.service;

import com.example.sarabrandserver.category.service.CategoryService;
import com.example.sarabrandserver.exception.CustomNotFoundException;
import com.example.sarabrandserver.product.dto.CreateProductDTO;
import com.example.sarabrandserver.product.dto.UpdateProductDTO;
import com.example.sarabrandserver.product.entity.*;
import com.example.sarabrandserver.product.repository.ProductDetailRepo;
import com.example.sarabrandserver.product.repository.ProductRepository;
import com.example.sarabrandserver.util.DateUTC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

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

    /***/
    public List<?> fetchAll() {
        return null;
    }

    /**
     * Method is responsible for saving a Product.
     * 1. Find ProductCategory since it has a 1 to many relationship with Product
     * 2. Add new ProductDetail to Product if Product name exists and return
     * 3. Save a new Product object and also save ProductCategory
     * 4. Add Image to Digital or AWS BUCKET
     * @param file of type MultipartFile
     * @param dto of type CreateProductDTO
     * @throws CustomNotFoundException is thrown when category name does not exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> create(CreateProductDTO dto, MultipartFile file) {
        // Find category by name
        var category = this.categoryService.findByName(dto.getCategoryName().trim());
        var date = this.dateUTC.toUTC(new Date()).isPresent() ? this.dateUTC.toUTC(new Date()).get() : new Date();

        var findProduct = this.productRepository.findByProductName(dto.getProductName().trim());
        if (findProduct.isPresent()) {
            var detail = new ProductDetail();
            detail.setModifiedAt(date);
            saveProduct(findProduct.get(), detail, dto, file);
            return new ResponseEntity<>("Added", OK);
        }

        // Create new ProductDetail
        var detail = new ProductDetail();
        detail.setCreateAt(date);
        // Create
        var product = new Product(dto.getProductName(), file.getOriginalFilename());
        // Save ProductDetail and Product
        saveProduct(product, detail, dto, file);

        // Add Product to ProductCategory since it is a 1 to many relationship between ProductCategory and Product
        category.addProduct(product);
        // Save category
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
        detail.setDescription(dto.getDesc().trim());
        detail.setSku(UUID.randomUUID().toString());
        detail.setQuantity(dto.getQty());
        detail.setPrice(dto.getPrice());
        detail.setCurrency(dto.getCurrency());

        // add image object
        detail.addImage(new ProductImage(file.getOriginalFilename()));
        // add size
        detail.addSize(new ProductSize(dto.getSize()));
        // add colour
        detail.addColour(new ProductColour(dto.getColour()));
        // save and add ProductDetail to Product
        product.addProductDetail(this.productDetailRepo.save(detail));

        this.productRepository.save(product);
    }

    /**
     * Method is responsible for updating a Product/ProductDetail
     * @param dto of type UpdateProductDTO
     * @param file of type MultipartFile
     * @throws CustomNotFoundException is thrown when Product name does not exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> updateProduct(final UpdateProductDTO dto, final  MultipartFile file) {
        var product = findProductByName(dto.getOldName().trim());


        return new ResponseEntity<>("Updated", OK);
    }

    /**
     * Method does not delete product, but it sets a Date attribute deleted_at
     * @param name is the Product name
     * @throws CustomNotFoundException is thrown when Product name does not exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> deleteProduct(final String name) {
        var product = findProductByName(name);

        return new ResponseEntity<>("Deleted", OK);
    }

    /**
     * Method custom deletes a ProductDetail from Product. For this to happen, all related map
     * @param name is the Product name
     * @param sku is a unique String for each ProductDetail
     * @throws CustomNotFoundException is thrown when Product name or SKU does not exist
     * @return ResponseEntity of type String
     * */
    @Transactional
    public ResponseEntity<?> deleteProductDetail(final String name, final String sku) {
        var product = findProductByName(name);
        ProductDetail detail = product.getProductDetails() //
                .stream() //
                .filter(prodDetail -> prodDetail.getSku().equals(sku))
                .findFirst() //
                .orElseThrow(() -> new  CustomNotFoundException("SKU does not exist"));

        var date = this.dateUTC.toUTC(new Date()).isPresent() ? this.dateUTC.toUTC(new Date()).get() : new Date();
        this.productDetailRepo.custom_delete(date, detail.getProductDetailId(), detail.getSku());
        return new ResponseEntity<>("Deleted", OK);
    }

    private Product findProductByName(String name) {
        return this.productRepository.findByProductName(name)
                .orElseThrow(() -> new CustomNotFoundException(name + " does not exist"));
    }

}

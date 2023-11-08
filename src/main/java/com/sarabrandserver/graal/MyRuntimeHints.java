package com.sarabrandserver.graal;

import com.sarabrandserver.auth.dto.LoginDTO;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.projection.CartPojo;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.sarabrandserver.category.projection.CategoryPojo;
import com.sarabrandserver.collection.dto.CollectionDTO;
import com.sarabrandserver.collection.dto.UpdateCollectionDTO;
import com.sarabrandserver.collection.projection.CollectionPojo;
import com.sarabrandserver.product.dto.*;
import com.sarabrandserver.product.projection.DetailPojo;
import com.sarabrandserver.product.projection.ImagePojo;
import com.sarabrandserver.product.projection.PriceCurrencyPojo;
import com.sarabrandserver.product.projection.ProductPojo;
import com.sarabrandserver.product.response.DetailResponse;
import com.sarabrandserver.product.response.Variant;
import com.sarabrandserver.util.VariantHelperMapper;
import com.sarabrandserver.util.CustomUtil;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * As per docs
 * <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html#native-image.advanced.custom-hints">...</a>
 * */
public class MyRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // migration
        hints.resources().registerPattern("db/migration/V6__init_migration.sql");
        hints.resources().registerPattern("db/migration/V7__init_migration.sql");
        hints.resources().registerPattern("db/migration/V8__init_migration.sql");

        // shopping session
        hints.serialization().registerType(CartDTO.class);

        // Auth
        hints.serialization().registerType(LoginDTO.class);
        hints.serialization().registerType(RegisterDTO.class);

        // Category
        hints.serialization().registerType(CategoryDTO.class);
        hints.serialization().registerType(UpdateCategoryDTO.class);

        // Collection
        hints.serialization().registerType(CollectionDTO.class);
        hints.serialization().registerType(UpdateCollectionDTO.class);

        // Product
        hints.serialization().registerType(CreateProductDTO.class);
        hints.serialization().registerType(ProductDetailDTO.class);
        hints.serialization().registerType(SizeInventoryDTO.class);
        hints.serialization().registerType(UpdateProductDetailDTO.class);
        hints.serialization().registerType(UpdateProductDTO.class);
        hints.serialization().registerType(PriceCurrencyDTO.class);

        // response
        hints.serialization().registerType(Variant.class);
        hints.serialization().registerType(DetailResponse.class);

        // variant
        hints.serialization().registerType(VariantHelperMapper.class);

        // spring data projection
        hints.serialization().registerType(CartPojo.class);
        hints.serialization().registerType(ProductPojo.class);
        hints.serialization().registerType(CategoryPojo.class);
        hints.serialization().registerType(CollectionPojo.class);
        hints.serialization().registerType(ImagePojo.class);
        hints.serialization().registerType(DetailPojo.class);
        hints.serialization().registerType(PriceCurrencyPojo.class);
    }

}

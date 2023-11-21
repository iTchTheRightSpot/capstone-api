package com.sarabrandserver.graal;

import com.sarabrandserver.auth.dto.LoginDTO;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.sarabrandserver.collection.dto.CollectionDTO;
import com.sarabrandserver.collection.dto.UpdateCollectionDTO;
import com.sarabrandserver.product.dto.*;
import com.sarabrandserver.product.response.DetailResponse;
import com.sarabrandserver.product.response.Variant;
import com.sarabrandserver.util.VariantHelperMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * As per docs
 * <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html#native-image.advanced.custom-hints">...</a>
 * */
public class MyRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // https://stackoverflow.com/questions/74829933/spring-boot-3-0-0-graavm-for-interface-jakarta-servlet-http-httpservletrequest
        hints.proxies().registerJdkProxy(HttpServletRequest.class);

        // migration
        hints.resources().registerPattern("db/migration/*");

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
        hints.serialization().registerType(VariantHelperMapper.class);
        hints.serialization().registerType(DetailResponse.class);
    }

}
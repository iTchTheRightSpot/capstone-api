package com.sarabrandserver.graal;

import com.sarabrandserver.auth.dto.LoginDTO;
import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.sarabrandserver.order.dto.OrderHistoryDTO;
import com.sarabrandserver.order.dto.PaymentDTO;
import com.sarabrandserver.order.dto.SkuQtyDTO;
import com.sarabrandserver.product.dto.*;
import com.sarabrandserver.product.response.DetailResponse;
import com.sarabrandserver.product.response.Variant;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * As per docs
 * <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html#native-image.advanced.custom-hints">...</a>
 * */
public class MyRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

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

        // Order
        hints.serialization().registerType(PaymentDTO.class);
        hints.serialization().registerType(OrderHistoryDTO.class);
        hints.serialization().registerType(SkuQtyDTO.class);
    }

}
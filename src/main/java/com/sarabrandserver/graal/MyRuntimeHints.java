package com.sarabrandserver.graal;

import com.sarabrandserver.auth.dto.LoginDto;
import com.sarabrandserver.auth.dto.RegisterDto;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.response.CartResponse;
import com.sarabrandserver.category.dto.CategoryDTO;
import com.sarabrandserver.category.dto.UpdateCategoryDTO;
import com.sarabrandserver.category.response.WorkerCategoryResponse;
import com.sarabrandserver.checkout.CheckoutPair;
import com.sarabrandserver.checkout.CustomObject;
import com.sarabrandserver.payment.dto.OrderHistoryDTO;
import com.sarabrandserver.payment.dto.PaymentDTO;
import com.sarabrandserver.payment.dto.SkuQtyDTO;
import com.sarabrandserver.product.dto.*;
import com.sarabrandserver.product.response.DetailResponse;
import com.sarabrandserver.product.response.Variant;
import com.sarabrandserver.shipping.ShippingDto;
import com.sarabrandserver.shipping.ShippingMapper;
import com.sarabrandserver.tax.TaxDto;
import com.sarabrandserver.thirdparty.PaymentCredentialObj;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * As per
 * <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html#native-image.advanced.custom-hints">docs</a>
 * */
public class MyRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // migration
        hints.resources().registerPattern("db/migration/*");

        // cart
        hints.serialization().registerType(CartDTO.class);
        hints.serialization().registerType(CartResponse.class);

        // Auth
        hints.serialization().registerType(LoginDto.class);
        hints.serialization().registerType(RegisterDto.class);

        // Category
        hints.serialization().registerType(CategoryDTO.class);
        hints.serialization().registerType(WorkerCategoryResponse.class);
        hints.serialization().registerType(UpdateCategoryDTO.class);

        // Product
        hints.serialization().registerType(CreateProductDTO.class);
        hints.serialization().registerType(ProductDetailDto.class);
        hints.serialization().registerType(SizeInventoryDTO.class);
        hints.serialization().registerType(UpdateProductDetailDto.class);
        hints.serialization().registerType(UpdateProductDTO.class);
        hints.serialization().registerType(PriceCurrencyDto.class);

        // response
        hints.serialization().registerType(Variant.class);
        hints.serialization().registerType(DetailResponse.class);

        // Order
        hints.serialization().registerType(PaymentDTO.class);
        hints.serialization().registerType(OrderHistoryDTO.class);
        hints.serialization().registerType(SkuQtyDTO.class);

        // shipping
        hints.serialization().registerType(ShippingDto.class);
        hints.serialization().registerType(ShippingMapper.class);

        // third-party package
        hints.serialization().registerType(PaymentCredentialObj.class);

        // Checkout
        hints.serialization().registerType(CheckoutPair.class);
        hints.serialization().registerType(CustomObject.class);

        // Tax
        hints.serialization().registerType(TaxDto.class);

    }

}
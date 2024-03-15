package dev.capstone.graal;

import dev.capstone.auth.dto.LoginDto;
import dev.capstone.auth.dto.RegisterDto;
import dev.capstone.cart.dto.CartDTO;
import dev.capstone.cart.response.CartResponse;
import dev.capstone.category.dto.CategoryDTO;
import dev.capstone.category.dto.UpdateCategoryDTO;
import dev.capstone.category.response.WorkerCategoryResponse;
import dev.capstone.checkout.CheckoutPair;
import dev.capstone.checkout.CustomObject;
import dev.capstone.payment.dto.OrderHistoryDTO;
import dev.capstone.payment.dto.PaymentDTO;
import dev.capstone.payment.dto.SkuQtyDTO;
import dev.capstone.product.dto.*;
import dev.capstone.product.response.DetailResponse;
import dev.capstone.product.response.Variant;
import dev.capstone.shipping.ShippingDto;
import dev.capstone.shipping.ShippingMapper;
import dev.capstone.tax.TaxDto;
import dev.capstone.thirdparty.PaymentCredentialObj;
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
package dev.webserver;

import dev.webserver.auth.dto.LoginDto;
import dev.webserver.auth.dto.RegisterDto;
import dev.webserver.cart.dto.CartDTO;
import dev.webserver.cart.response.CartResponse;
import dev.webserver.category.dto.CategoryDTO;
import dev.webserver.category.dto.UpdateCategoryDTO;
import dev.webserver.category.response.WorkerCategoryResponse;
import dev.webserver.checkout.CheckoutPair;
import dev.webserver.checkout.CustomObject;
import dev.webserver.external.log.DiscordPayload;
import dev.webserver.payment.dto.OrderHistoryDTO;
import dev.webserver.payment.dto.PaymentDTO;
import dev.webserver.payment.dto.SkuQtyDTO;
import dev.webserver.payment.response.PaymentResponse;
import dev.webserver.payment.util.WebhookAuthorization;
import dev.webserver.payment.util.WebhookConstruct;
import dev.webserver.payment.util.WebhookMetaData;
import dev.webserver.product.dto.*;
import dev.webserver.product.response.DetailResponse;
import dev.webserver.product.response.Variant;
import dev.webserver.shipping.ShippingDto;
import dev.webserver.shipping.ShippingMapper;
import dev.webserver.tax.TaxDto;
import dev.webserver.external.PaymentCredentialObj;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * As per
 * <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html#native-image.advanced.custom-hints">docs</a>
 * */
final class MyRuntimeHints implements RuntimeHintsRegistrar {

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

        // Payment
        hints.serialization().registerType(PaymentResponse.class);

        // Webhook
        hints.serialization().registerType(WebhookMetaData.class);
        hints.serialization().registerType(WebhookAuthorization.class);
        hints.serialization().registerType(WebhookConstruct.class);

        // logs
        hints.serialization().registerType(DiscordPayload.class);
    }

}
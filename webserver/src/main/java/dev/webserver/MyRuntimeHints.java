package dev.webserver;

import dev.webserver.cart.CartDto;
import dev.webserver.cart.CartResponse;
import dev.webserver.category.CategoryDto;
import dev.webserver.category.UpdateCategoryDto;
import dev.webserver.category.WorkerCategoryResponse;
import dev.webserver.checkout.CheckoutPair;
import dev.webserver.checkout.CustomObject;
import dev.webserver.external.payment.PaymentCredentialObj;
import dev.webserver.external.log.DiscordPayload;
import dev.webserver.payment.OrderHistoryDto;
import dev.webserver.payment.PaymentDTO;
import dev.webserver.payment.SkuQtyDto;
import dev.webserver.payment.PaymentResponse;
import dev.webserver.payment.WebhookAuthorization;
import dev.webserver.payment.WebhookConstruct;
import dev.webserver.payment.WebhookMetaData;
import dev.webserver.product.*;
import dev.webserver.product.util.CustomMultiPart;
import dev.webserver.product.DetailResponse;
import dev.webserver.product.util.Variant;
import dev.webserver.security.controller.LoginDto;
import dev.webserver.security.controller.RegisterDto;
import dev.webserver.shipping.ShippingDto;
import dev.webserver.shipping.ShippingMapper;
import dev.webserver.tax.TaxDto;
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

        hints.serialization()
                .registerType(CartDto.class)
                .registerType(CartResponse.class)
                .registerType(LoginDto.class)
                .registerType(RegisterDto.class)
                .registerType(CategoryDto.class)
                .registerType(WorkerCategoryResponse.class)
                .registerType(UpdateCategoryDto.class)
                .registerType(CreateProductDto.class)
                .registerType(ProductDetailDto.class)
                .registerType(SizeInventoryDto.class)
                .registerType(UpdateProductDetailDto.class)
                .registerType(UpdateProductDto.class)
                .registerType(PriceCurrencyDto.class)
                .registerType(Variant.class)
                .registerType(DetailResponse.class)
                .registerType(PaymentDTO.class)
                .registerType(OrderHistoryDto.class)
                .registerType(SkuQtyDto.class)
                .registerType(ShippingDto.class)
                .registerType(ShippingMapper.class)
                .registerType(PaymentCredentialObj.class)
                .registerType(CheckoutPair.class)
                .registerType(CustomObject.class)
                .registerType(TaxDto.class)
                .registerType(PaymentResponse.class)
                .registerType(WebhookMetaData.class)
                .registerType(WebhookAuthorization.class)
                .registerType(WebhookConstruct.class)
                .registerType(DiscordPayload.class)
                .registerType(CustomMultiPart.class)
                .registerType(ProductResponse.class);
    }

}
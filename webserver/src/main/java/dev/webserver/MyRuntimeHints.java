package dev.webserver;

import dev.webserver.cart.CartDto;
import dev.webserver.cart.CartResponse;
import dev.webserver.category.CategoryDto;
import dev.webserver.category.UpdateCategoryDto;
import dev.webserver.external.log.DiscordPayload;
import dev.webserver.payment.*;
import dev.webserver.product.*;
import dev.webserver.security.controller.ActiveUser;
import dev.webserver.security.demo.LoginDto;
import dev.webserver.shipping.ShippingDto;
import dev.webserver.shipping.ShippingMapper;
import dev.webserver.tax.TaxDto;
import dev.webserver.util.Page;
import dev.webserver.util.Pageable;
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
                .registerType(ActiveUser.class)
                .registerType(CategoryDto.class)
                .registerType(UpdateCategoryDto.class)
                .registerType(CreateProductDto.class)
                .registerType(ProductDetailDto.class)
                .registerType(SizeInventoryDto.class)
                .registerType(UpdateProductDetailDto.class)
                .registerType(UpdateProductDto.class)
                .registerType(PriceCurrencyDto.class)
                .registerType(Variant.class)
                .registerType(DetailResponse.class)
                .registerType(PaymentDto.class)
                .registerType(OrderHistoryDto.class)
                .registerType(SkuQtyDto.class)
                .registerType(ShippingDto.class)
                .registerType(ShippingMapper.class)
                .registerType(AbstractEnvironment.PaymentCredentialObj.class)
                .registerType(CheckoutPair.class)
                .registerType(CustomCheckoutObject.class)
                .registerType(TaxDto.class)
                .registerType(PaymentResponse.class)
                .registerType(WebhookMetaData.class)
                .registerType(WebhookAuthorization.class)
                .registerType(WebhookConstruct.class)
                .registerType(DiscordPayload.class)
                .registerType(CustomMultiPart.class)
                .registerType(ProductResponse.class)
                .registerType(CronJob.CustomCronJobObject.class)
                .registerType(LoginDto.class)
                .registerType(Pageable.class)
                .registerType(Page.class);
    }
}
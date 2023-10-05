package com.sarabrandserver.graal;

import com.sarabrandserver.product.dto.SizeInventoryDTO;
import com.sarabrandserver.store.HomeResponse;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

/**
 * As per docs
 * <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html#native-image.advanced.custom-hints">...</a>
 * */
public class MyRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.serialization().registerType(TypeReference.of(SizeInventoryDTO.class));
        hints.serialization().registerType(HomeResponse.class);
    }

}

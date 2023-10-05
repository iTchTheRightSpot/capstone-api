package com.sarabrandserver.product;

import com.sarabrandserver.product.dto.SizeInventoryDTO;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

public class SizeInvDTORuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.serialization().registerType(TypeReference.of(SizeInventoryDTO.class));
    }

}

package com.emmanuel.sarabrandserver.util;

import com.emmanuel.sarabrandserver.exception.InvalidFormat;
import com.emmanuel.sarabrandserver.product.util.SizeInventoryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * The purpose of this class is to handle Nested DTO in CreateProductDTO.
 * Converts json sent as a String[] to SizeInventoryDTO[]. As per docs
 * <a href="https://docs.spring.io/spring-framework/reference/core/validation/convert.html">...</a>
 */
@Component
public class SizeInventoryDTOConverter implements Converter<String[], SizeInventoryDTO[]> {
    private static final Logger log = Logger.getLogger(SizeInventoryDTOConverter.class.getName());

    private final ObjectMapper objectMapper;

    public SizeInventoryDTOConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public SizeInventoryDTO[] convert(String @NotNull(message = "size or quantity cannot be null") [] source) {
        return getSizeInventoryDTOS(source, this.objectMapper);
    }

    @NotNull
    static SizeInventoryDTO[] getSizeInventoryDTOS(
            String @NotNull(message = "size or quantity cannot be null") [] source,
            ObjectMapper objectMapper
    ) {
        SizeInventoryDTO[] dto = new SizeInventoryDTO[source.length];

        for (int i = 0; i < source.length; i++) {
            String str = source[i];
            try {
                dto[i] = objectMapper.readValue(str, SizeInventoryDTO.class);
            } catch (Exception e) {
                log.info("Incorrect format SizeInventoryDTOConverter. " + e.getMessage());
                throw new InvalidFormat("Invalid size and inventory");
            }
        }

        return dto;
    }
}

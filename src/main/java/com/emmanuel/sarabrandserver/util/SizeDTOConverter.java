package com.emmanuel.sarabrandserver.util;

import com.emmanuel.sarabrandserver.exception.InvalidFormat;
import com.emmanuel.sarabrandserver.product.util.SizeInventoryDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/** Incase there is only one item instead of a string[] */
@Component
public class SizeDTOConverter implements Converter<String, SizeInventoryDTO[]> {
    private static final Logger log = Logger.getLogger(SizeDTOConverter.class.getName());

    private final ObjectMapper objectMapper;

    public SizeDTOConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public SizeInventoryDTO[] convert(@NotNull String source) {
        try {
            return new SizeInventoryDTO[]{ objectMapper.readValue(source, SizeInventoryDTO.class) };
        } catch (JsonProcessingException e) {
            log.info("Incorrect format SizeInventoryDTOConverter. " + e.getMessage());
            throw new InvalidFormat("Please enter a size and quantity for product");
        }
    }

}

package dev.webserver.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DetailResponse (
        String name,
        String currency,
        BigDecimal price,
        String desc,
        @JsonProperty(value = "visible")
        Boolean isVisible,
        String colour,
        @JsonProperty("image_keys")
        List<String> imageKeys,
        Variant[] variants
) implements Serializable {}
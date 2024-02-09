package com.sarabrandserver.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Acts as an updateDto and Shipping response mapper.
 * */
public class ShippingMapper extends ShippingDto {

        @NotNull
        @JsonProperty("shipping_id")
        private final Long id;

        public ShippingMapper(Long id, String country, BigDecimal ngn, BigDecimal usd) {
                super(country, ngn, usd);
                this.id = id;
        }

        public Long id() {
                return id;
        }

}
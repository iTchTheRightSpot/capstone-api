package com.sarabrandserver.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ShippingDto {

        @NotNull
        @NotEmpty
        private final String country;
        @NotNull
        @JsonProperty("ngn_price")
        private final BigDecimal ngn;
        @NotNull
        @JsonProperty("usd_price")
        private final BigDecimal usd;

        public ShippingDto(String country, BigDecimal ngn, BigDecimal usd) {
                this.country = country;
                this.ngn = ngn;
                this.usd = usd;
        }

        public String country() {
                return country;
        }

        public BigDecimal ngn() {
                return ngn;
        }

        public BigDecimal usd() {
                return usd;
        }

}
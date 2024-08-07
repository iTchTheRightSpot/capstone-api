package dev.webserver.payment;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;

@Table(name = "address")
@Builder
record Address(
        @Id
        @Column("address_id")
        Long addressId,
        String address,
        String city,
        String state,
        String postcode,
        String country,
        @Column("delivery_info")
        String deliveryInfo
) {
}

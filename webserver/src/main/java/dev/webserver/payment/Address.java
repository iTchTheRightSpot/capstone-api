package dev.webserver.payment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "address")
@Builder
record Address(
        @Id
        @Column("address_id")
        Long addressId,
        @NotNull(message = "address cannot be null")
        @NotEmpty(message = "address cannot be empty")
        @Size.List({@Size(max = 300, message = "address max length of 300")})
        String address,
        @NotNull(message = "address city cannot be null")
        @NotEmpty(message = "address city cannot be empty")
        @Size.List({@Size(max = 255, message = "address city max length of 255")})
        String city,
        @NotNull(message = "address state cannot be null")
        @NotEmpty(message = "address state cannot be empty")
        @Size.List({@Size(max = 100, message = "address state max length of 100")})
        String state,
        @Size.List({@Size(max = 10, message = "address postcode max length of 10")})
        String postcode,
        @NotNull(message = "address country cannot be null")
        @NotEmpty(message = "address country cannot be empty")
        @Size.List({@Size(max = 100, message = "address country max length of 100")})
        String country,
        @Column("delivery_info")
        @Size.List({@Size(max = 1000, message = "address delivery_info max length of 1000")})
        String deliveryInfo
) {
}

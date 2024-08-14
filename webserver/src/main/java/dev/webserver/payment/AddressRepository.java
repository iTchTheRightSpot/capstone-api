package dev.webserver.payment;

import dev.webserver.exception.CustomServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AddressRepository {
    private final JdbcClient client;

    public Address save(final Address address) {
        final int update = client.sql("INSERT INTO address (address_id, address, city, state, postcode, country, delivery_info) VALUE (?, ?, ?, ?, ?, ?, ?)")
                .param(1, address.addressId())
                .param(2, address.address())
                .param(3, address.city())
                .param(4, address.state())
                .param(5, address.postcode())
                .param(6, address.country())
                .param(7, address.deliveryInfo())
                .update();

        if (update < 1) throw new CustomServerException("error saving Address");

        return address;
    }

    public List<Address> findAll() {
        return client.sql("SELECT * FROM address").query(Address.class).list();
    }
}
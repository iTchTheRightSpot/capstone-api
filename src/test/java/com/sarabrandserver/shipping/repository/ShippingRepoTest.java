package com.sarabrandserver.shipping.repository;

import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.shipping.entity.Shipping;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ShippingRepoTest extends AbstractRepositoryTest {

    @Autowired
    private ShippingRepo shippingRepo;

    @Test
    void deleteShippingById() {
        // given
        var obj = shippingRepo
                .save(new Shipping("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));
        long id = obj.shippingId();

        // when
        shippingRepo.deleteShippingById(id);

        // then
        assertTrue(shippingRepo.findById(id).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenCreatingAnExistingCountry() {
        // given
        shippingRepo
                .save(new Shipping("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));

        // then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> shippingRepo
                        .save(new Shipping(
                                "nigeria",
                                new BigDecimal("5000"),
                                new BigDecimal("5.50")
                        ))
        );
    }

    @Test
    void updateShippingById() {
        // given
        var obj = shippingRepo
                .save(new Shipping("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));
        long id = obj.shippingId();

        // when
        shippingRepo
                .updateShippingById(id, "france", new BigDecimal("500"), new BigDecimal("5.50"));

        // then
        var optional = shippingRepo.findById(id);
        assertFalse(optional.isEmpty());

        var shipping = optional.get();
        assertEquals("france", shipping.country());
        assertEquals(new BigDecimal("500.00"), shipping.ngnPrice());
        assertEquals(new BigDecimal("5.50"), shipping.usdPrice());
    }

    @Test
    void shouldThrowErrorWhenUpdatingACountry() {
        // given
        var obj = shippingRepo
                .save(new Shipping("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));
        long id = obj.shippingId();

        shippingRepo
                .save(new Shipping("france", new BigDecimal("4500"), new BigDecimal("3.50")));

        // then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> shippingRepo
                        .updateShippingById(
                                id,
                                "france",
                                new BigDecimal("500"),
                                new BigDecimal("5.50")
                        )
        );
    }

    @Test
    void shouldNotReturnDefaultShipping() {
        // given
        var obj = shippingRepo
                .save(new Shipping("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));

        // when
        var optional = shippingRepo
                .shippingByCountryElseReturnDefault(obj.country());

        // then
        assertFalse(optional.isEmpty());
        assertEquals(obj, optional.get());
    }

    @Test
    void shouldReturnDefaultShippingInsertedInMigrationScriptV13() {
        // given
        shippingRepo
                .save(new Shipping("france", new BigDecimal("4500"), new BigDecimal("3.50")));

        // when
        var optional = shippingRepo
                .shippingByCountryElseReturnDefault("nigeria");

        // then
        assertFalse(optional.isEmpty());

        Shipping obj = optional.get();
        assertEquals(obj.shippingId(), 1L);
        assertEquals(obj.country(), "default");
        assertEquals(obj.ngnPrice(), new BigDecimal("0.00"));
        assertEquals(obj.ngnPrice(), new BigDecimal("0.00"));
    }

}
package dev.webserver.shipping.repository;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.shipping.entity.ShipSetting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ShippingRepoTest extends AbstractRepositoryTest {

    @Autowired
    private ShippingRepo shippingRepo;

    @Test
    void deleteShipSettingById() {
        // given
        var obj = shippingRepo
                .save(new ShipSetting("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));
        long id = obj.shipId();

        // when
        shippingRepo.deleteShipSettingById(id);

        // then
        assertTrue(shippingRepo.findById(id).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenCreatingAnExistingShipSettingPropertyCountry() {
        // given
        shippingRepo
                .save(new ShipSetting("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));

        // then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> shippingRepo
                        .save(new ShipSetting(
                                "nigeria",
                                new BigDecimal("5000"),
                                new BigDecimal("5.50")
                        ))
        );
    }

    @Test
    void updateShipSettingById() {
        // given
        var obj = shippingRepo
                .save(new ShipSetting("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));
        long id = obj.shipId();

        // when
        shippingRepo
                .updateShipSettingById(id, "france", new BigDecimal("500"), new BigDecimal("5.50"));

        // then
        var optional = shippingRepo.findById(id);
        assertFalse(optional.isEmpty());

        var shipping = optional.get();
        Assertions.assertEquals("france", shipping.country());
        Assertions.assertEquals(new BigDecimal("500.00"), shipping.ngnPrice());
        Assertions.assertEquals(new BigDecimal("5.50"), shipping.usdPrice());
    }

    @Test
    void shouldThrowErrorWhenUpdatingShipSettingPropertyCountryToExistingCountry() {
        // given
        var obj = shippingRepo
                .save(new ShipSetting("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));
        long id = obj.shipId();

        shippingRepo
                .save(new ShipSetting("france", new BigDecimal("4500"), new BigDecimal("3.50")));

        // then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> shippingRepo
                        .updateShipSettingById(
                                id,
                                "france",
                                new BigDecimal("500"),
                                new BigDecimal("5.50")
                        )
        );
    }

    @Test
    void shouldNotUpdateDefaultShipSettingPropertyCountryButUpdateOtherProperties() {
        // when
        shippingRepo
                .updateShipSettingById(
                        1,
                        "france",
                        new BigDecimal("500"),
                        new BigDecimal("5.50")
                );

        var optional = shippingRepo.findById(1L);
        assertFalse(optional.isEmpty());

        // then
        ShipSetting ship = optional.get();
        assertEquals("default", ship.country());
        assertEquals(new BigDecimal("500.00"), ship.ngnPrice());
        assertEquals(new BigDecimal("5.50"), ship.usdPrice());
    }

    @Test
    void shouldNotReturnDefaultShipSetting() {
        // given
        var obj = shippingRepo
                .save(new ShipSetting("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));

        // when
        var optional = shippingRepo
                .shippingByCountryElseReturnDefault(obj.country());

        // then
        assertFalse(optional.isEmpty());
        Assertions.assertEquals(obj, optional.get());
    }

    @Test
    void shouldReturnDefaultShipSettingInsertedInMigrationScriptV13() {
        // given
        shippingRepo
                .save(new ShipSetting("france", new BigDecimal("4500"), new BigDecimal("3.50")));

        // when
        var optional = shippingRepo
                .shippingByCountryElseReturnDefault("nigeria");

        // then
        assertFalse(optional.isEmpty());

        ShipSetting obj = optional.get();
        assertEquals(obj.shipId(), 1L);
        assertEquals(obj.country(), "default");
        assertEquals(obj.ngnPrice(), new BigDecimal("0.00"));
        assertEquals(obj.ngnPrice(), new BigDecimal("0.00"));
    }

}
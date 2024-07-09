package dev.webserver.shipping;

import dev.webserver.AbstractRepositoryTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ShippingRepoTest extends AbstractRepositoryTest {

    @Autowired
    private ShippingRepository shippingRepository;

    @Test
    void deleteShipSettingById() {
        // given
        var obj = shippingRepository
                .save(new ShipSetting("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));
        long id = obj.shipId();

        // when
        shippingRepository.deleteShipSettingById(id);

        // then
        assertTrue(shippingRepository.findById(id).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenCreatingAnExistingShipSettingPropertyCountry() {
        // given
        shippingRepository
                .save(new ShipSetting("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));

        // then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> shippingRepository
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
        var obj = shippingRepository
                .save(new ShipSetting("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));
        long id = obj.shipId();

        // when
        shippingRepository
                .updateShipSettingById(id, "france", new BigDecimal("500"), new BigDecimal("5.50"));

        // then
        var optional = shippingRepository.findById(id);
        assertFalse(optional.isEmpty());

        var shipping = optional.get();
        Assertions.assertEquals("france", shipping.country());
        Assertions.assertEquals(new BigDecimal("500.00"), shipping.ngnPrice());
        Assertions.assertEquals(new BigDecimal("5.50"), shipping.usdPrice());
    }

    @Test
    void shouldThrowErrorWhenUpdatingShipSettingPropertyCountryToExistingCountry() {
        // given
        var obj = shippingRepository
                .save(new ShipSetting("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));
        long id = obj.shipId();

        shippingRepository
                .save(new ShipSetting("france", new BigDecimal("4500"), new BigDecimal("3.50")));

        // then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> shippingRepository
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
        shippingRepository
                .updateShipSettingById(
                        1,
                        "france",
                        new BigDecimal("500"),
                        new BigDecimal("5.50")
                );

        var optional = shippingRepository.findById(1L);
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
        var obj = shippingRepository
                .save(new ShipSetting("nigeria", new BigDecimal("4500"), new BigDecimal("3.50")));

        // when
        var optional = shippingRepository
                .shippingByCountryElseReturnDefault(obj.country());

        // then
        assertFalse(optional.isEmpty());
        Assertions.assertEquals(obj, optional.get());
    }

    @Test
    void shouldReturnDefaultShipSettingInsertedInMigrationScriptV13() {
        // given
        shippingRepository
                .save(new ShipSetting("france", new BigDecimal("4500"), new BigDecimal("3.50")));

        // when
        var optional = shippingRepository
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
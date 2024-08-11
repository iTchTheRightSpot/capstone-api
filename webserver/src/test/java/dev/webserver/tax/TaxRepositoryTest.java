package dev.webserver.tax;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.TestUtility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

final class TaxRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TaxRepository repository;

    @Test
    void shouldContainDefaultTaxAsPerMigrationScriptV15() {
        // when
        final var all = TestUtility.toList(repository.findAll());

        // then
        assertEquals(1, all.size());
    }

    @Test
    void updateTaxByTaxId () {
        // when
        repository.updateTaxByTaxId(1, "name", new BigDecimal("25.3200"));

        // then
        final var optional = repository.findById(1L);
        assertFalse(optional.isEmpty());

        final Tax tax = optional.get();
        assertEquals("name", tax.name());
        assertEquals(new BigDecimal("25.3200"), tax.rate());
    }

    @Test
    void shouldThrowErrorAsTaxRateIsNotInTheRightFormat() {
        // when
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.updateTaxByTaxId(1, "name", new BigDecimal("225.32")));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.updateTaxByTaxId(1, "frank", new BigDecimal("225.32666")));
    }

    @Test
    void shouldThrowErrorWhenUpdatingTaxBecauseOfLengthOfName() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.updateTaxByTaxId(1,"hungary-tax", new BigDecimal("10.2345")));
    }

}
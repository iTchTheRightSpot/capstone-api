package dev.webserver.tax;

import dev.webserver.AbstractRepositoryTest;
import dev.webserver.TestUtility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

class TaxRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TaxRepository repository;

    @Test
    void shouldContainDefaultTaxAsPerMigrationScriptV15() {
        // when
        var all = TestUtility.toList(repository.findAll());

        // then
        assertEquals(1, all.size());
    }

    @Test
    void updateTaxByTaxId () {
        // when
        repository.updateTaxByTaxId(1, "name", 25.32);

        // then
        var optional = repository.findById(1L);
        assertFalse(optional.isEmpty());

        Tax tax = optional.get();
        assertEquals("name", tax.name());
        assertEquals(25.32, tax.rate());
    }

    @Test
    void shouldThrowErrorAsTaxRateIsNotInTheRightFormat() {
        // when
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.updateTaxByTaxId(1, "name", 225.32));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.updateTaxByTaxId(1, "frank", 225.32666));
    }

    @Test
    void shouldThrowErrorWhenUpdatingTaxBecauseOfLengthOfName() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.updateTaxByTaxId(1,"hungary-tax", 10.2345));
    }

}
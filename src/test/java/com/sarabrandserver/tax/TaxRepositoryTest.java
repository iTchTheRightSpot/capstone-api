package com.sarabrandserver.tax;

import com.sarabrandserver.AbstractRepositoryTest;
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
        var all = repository.findAll();

        // then
        assertEquals(1, all.size());
    }

    @Test
    void updateByTaxId () {
        // when
        repository.updateByTaxId(1, "name", 25.32);

        // then
        var optional = repository.findById(1L);
        assertFalse(optional.isEmpty());

        Tax tax = optional.get();
        assertEquals("name", tax.name());
        assertEquals(25.32, tax.percentage());
    }

    @Test
    void shouldThrowErrorAsPercentageIsNotInTheRightFormat() {
        // when
        assertThrows(DataIntegrityViolationException.class,
                () -> repository
                        .updateByTaxId(1, "name", 225.32));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository
                        .updateByTaxId(1, "frank", 225.32666));
    }

    @Test
    void shouldThrowErrorWhenUpdatingTaxBecauseOfLengthOfName() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.updateByTaxId(1,"hungary-tax", 10.2345)
        );
    }

}
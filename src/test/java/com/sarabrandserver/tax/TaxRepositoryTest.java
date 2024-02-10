package com.sarabrandserver.tax;

import com.sarabrandserver.AbstractRepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class TaxRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TaxRepository repository;

    @Test
    void updateByTaxId () {
        // given
        // contains value from migration script V15
        var all = repository.findAll();
        assertEquals(1, all.size());
        Tax first = all.getFirst();

        // when
        repository
                .updateByTaxId(first.taxId(), "name", 25.32);

        // then
        var optional = repository.findById(first.taxId());
        assertFalse(optional.isEmpty());

        Tax tax = optional.get();
        assertEquals("name", tax.name());
        assertEquals(25.32, tax.percentage());
    }

}
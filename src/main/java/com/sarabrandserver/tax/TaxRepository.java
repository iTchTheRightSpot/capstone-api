package com.sarabrandserver.tax;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TaxRepository extends JpaRepository<Tax, Long> {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query ("""
    UPDATE Tax t
    SET t.name = :name, t.rate = :percentage
    WHERE t.taxId = :id
    """)
    void updateTaxByTaxId(long id, String name, double rate);

}
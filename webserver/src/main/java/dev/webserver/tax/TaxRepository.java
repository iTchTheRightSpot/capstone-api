package dev.webserver.tax;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface TaxRepository extends CrudRepository<Tax, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE tax t SET t.name = :name, t.rate = :rate WHERE t.taxId = :id")
    void updateTaxByTaxId(long id, String name, double rate);

}
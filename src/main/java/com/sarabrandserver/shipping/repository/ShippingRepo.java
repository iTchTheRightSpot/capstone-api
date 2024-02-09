package com.sarabrandserver.shipping.repository;

import com.sarabrandserver.shipping.entity.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ShippingRepo extends JpaRepository<Shipping, Long> {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Shipping s WHERE s.shippingId = :id")
    void deleteShippingById(long id);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    UPDATE Shipping s
    SET s.country = :name, s.ngnPrice = :ngn, s.usdPrice = :usd
    WHERE s.shippingId = :id
    """)
    void updateShippingById(long id, String country, BigDecimal ngn, BigDecimal usd);

    /**
     * Retrieves a {@code Shipping} entity based on the provided country.
     * If a country is specified, it returns the corresponding
     * {@code Shipping} entity, otherwise, it returns a default entity
     * where the country name is 'default'.
     *
     * @param country The country for which to retrieve the
     *                {@code Shipping} entity. If {@code Shipping} does
     *                not exist, a default entity with country 'default'
     *                is returned.
     * @return An {@code Optional} containing the retrieved {@code Shipping}
     * entity, or an empty {@code Optional} if default entity is not found.
     */
    @Query("""
    SELECT
    CASE
        WHEN s.country = :country THEN s
        WHEN s.country = 'default' THEN s
    END
    FROM Shipping s
    """)
    Optional<Shipping> shippingByCountryElseReturnDefault(String country);

}
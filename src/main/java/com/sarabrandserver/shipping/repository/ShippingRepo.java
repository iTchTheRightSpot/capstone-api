package com.sarabrandserver.shipping.repository;

import com.sarabrandserver.shipping.entity.ShipSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ShippingRepo extends JpaRepository<ShipSetting, Long> {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM ShipSetting s WHERE s.shipId = :id")
    void deleteShipSettingById(long id);

    /**
     * Updates {@code ShipSetting} by categoryId with the provided country and prices.
     * <p>
     * This method updates the {@code ShipSetting} identified by the given categoryId with the
     * specified country and prices. If the categoryId matches a default value entered in
     * db/migration/V13 set country to 'default'; otherwise, the provided country is used.
     *
     * @param id The ID of the shipping settings to update.
     * @param country The country to set for the shipping settings.
     * @param ngn The price in NGN (Nigerian Naira) to set for the shipping settings.
     * @param usd The price in USD (United States Dollar) to set for the shipping settings.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    UPDATE ShipSetting s
    SET
    s.country = (
        CASE WHEN
            1 = :id
            THEN 'default'
            ELSE :country
            END
    ),
    s.ngnPrice = :ngn,
    s.usdPrice = :usd
    WHERE s.shipId = :id
    """)
    void updateShipSettingById(long id, String country, BigDecimal ngn, BigDecimal usd);

    /**
     * Retrieves a {@code Shipping} entity based on the provided country.
     * If a country is specified, it returns the corresponding
     * {@code Shipping} entity, otherwise, it returns a default entity
     * where the country name is 'default'. For better understanding,
     * the native query is:
     * SELECT * FROM shipping s WHERE s.country = (
     * IF(( SELECT COUNT(*) FROM shipping h WHERE h.country = :country ) > 0,
     * :country, 'default'))
     *
     * @param country The country for which to retrieve the
     *                {@code Shipping} entity. If {@code Shipping} does
     *                not exist, a default entity with country 'default'
     *                is returned.
     * @return An {@code Optional} containing the retrieved {@code Shipping}
     * entity, or an empty {@code Optional} if default entity is not found.
     */
    @Query("""
    SELECT s FROM ShipSetting s
    WHERE s.country = (
        CASE WHEN (
                SELECT
                COUNT(s)
                FROM ShipSetting s
                WHERE s.country = :country
            ) > 0
            THEN :country
            ELSE 'default'
            END
    )
    """)
    Optional<ShipSetting> shippingByCountryElseReturnDefault(String country);

}
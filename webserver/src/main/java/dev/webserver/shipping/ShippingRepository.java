package dev.webserver.shipping;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

public interface ShippingRepository extends CrudRepository<ShipSetting, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM ship_setting s WHERE s.ship_id = :id")
    void deleteShipSettingById(long id);

    /**
     * Updates {@link ShipSetting} by categoryId with the provided country and prices.
     * <p>
     * This method updates the {@link ShipSetting} identified by the given categoryId with the
     * specified country and prices. If the categoryId matches a default value entered in
     * db/migration/V13 set country to 'default'; otherwise, the provided country is used.
     *
     * @param id The ID of the shipping settings to update.
     * @param country The country to set for the shipping settings.
     * @param ngn The price in NGN (Nigerian Naira) to set for the shipping settings.
     * @param usd The price in USD (United States Dollar) to set for the shipping settings.
     */
    @Transactional
    @Modifying
    @Query("""
    UPDATE ship_setting s
    SET
    s.country = (
        CASE WHEN
            1 = :id
            THEN 'default'
            ELSE :country
            END
    ),
    s.ngn_price = :ngn,
    s.usd_price = :usd
    WHERE s.ship_id = :id
    """)
    void updateShipSettingById(long id, String country, BigDecimal ngn, BigDecimal usd);

    /**
     * Retrieves a {@link ShipSetting} entity based on the provided country.
     * If a country is specified, it returns the corresponding
     * {@link ShipSetting} entity, otherwise, it returns a default entity
     * where the country name is 'default'. For better understanding,
     * the native query is:
     * SELECT * FROM shipping s WHERE s.country = (
     * IF(( SELECT COUNT(*) FROM shipping h WHERE h.country = :country ) > 0,
     * :country, 'default'))
     *
     * @param country The country for which to retrieve the
     *                {@link ShipSetting} entity. If {@link ShipSetting} does
     *                not exist, a default entity with country 'default'
     *                is returned.
     * @return An {@link Optional} containing the retrieved {@code Shipping}
     * entity, or an empty {@link Optional} if default entity is not found.
     */
    @Query("""
    SELECT * FROM ship_setting s
    WHERE s.country = (
        CASE WHEN (
                SELECT
                COUNT(s)
                FROM ship_setting s
                WHERE s.country = :country
            ) > 0
            THEN :country
            ELSE 'default'
            END
    )
    """)
    Optional<ShipSetting> shippingByCountryElseReturnDefault(String country);

}
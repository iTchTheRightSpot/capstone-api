package com.sarabrandserver.shipping.repository;

import com.sarabrandserver.enumeration.ShippingType;
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
    void deleteByShippingId(long id);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    UPDATE Shipping s
    SET s.ngnPrice = :ngn, s.usdPrice = :usd
    WHERE s.shippingId = :id
    """)
    void updateByShippingId(long id, BigDecimal ngn, BigDecimal usd);

    @Query("SELECT s FROM Shipping s WHERE s.shippingType = :type")
    Optional<Shipping> findByShippingType(ShippingType type);

}
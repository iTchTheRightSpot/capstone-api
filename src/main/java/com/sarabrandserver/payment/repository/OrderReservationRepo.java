package com.sarabrandserver.payment.repository;

import com.sarabrandserver.enumeration.ReservationStatus;
import com.sarabrandserver.payment.entity.OrderReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Contains native query
 * */
@Repository
public interface OrderReservationRepo extends JpaRepository<OrderReservation, Long> {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
    UPDATE product_sku s
    INNER JOIN order_reservation o ON s.sku_id = o.sku_id
    INNER JOIN shopping_session sh ON o.session_id = sh.session_id
    SET
    s.inventory = (s.inventory - :productSkuQty),
    o.qty = :reservationQty,
    o.expire_at = :expire
    WHERE s.sku = :sku AND sh.cookie = :cookie AND o.status = :#{#status.name()}
    """)
    void onSub(
            int productSkuQty,
            int reservationQty,
            Date expire,
            String cookie,
            String sku,
            @Param(value = "status") ReservationStatus status
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
    UPDATE product_sku s
    INNER JOIN order_reservation o ON s.sku_id = o.sku_id
    INNER JOIN shopping_session sh ON o.session_id = sh.session_id
    SET
    s.inventory = (s.inventory + :productSkuQty),
    o.qty = :reservationQty,
    o.expire_at = :expire
    WHERE s.sku = :sku AND sh.cookie = :cookie AND o.status = :#{#status.name()}
    """)
    void onAdd(
            int productSkuQty,
            int reservationQty,
            Date expire,
            String cookie,
            String sku,
            @Param(value = "status") ReservationStatus status
    );

    @Query("SELECT o FROM OrderReservation o WHERE o.expireAt <= :date AND o.status = :status")
    List<OrderReservation> allPendingExpiredReservations(Date date, ReservationStatus status);

    @Query("SELECT o FROM OrderReservation o WHERE o.expireAt > :date AND o.status = :status")
    List<OrderReservation> allPendingNoneExpiredReservations(Date date, ReservationStatus status);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM OrderReservation o WHERE o.reservationId = :id")
    void deleteOrderReservationByReservationId(long id);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM OrderReservation o WHERE o.expireAt <= :date")
    void deleteExpired(Date date);

}
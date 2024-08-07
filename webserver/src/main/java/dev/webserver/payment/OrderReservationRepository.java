package dev.webserver.payment;

import dev.webserver.cart.ShoppingSession;
import dev.webserver.enumeration.ReservationStatus;
import dev.webserver.product.ProductSku;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderReservationRepository extends CrudRepository<OrderReservation, Long> {

    /**
     * Updates a {@link ProductSku} by adding to its existing inventory and replaces
     * the qty of a {@link OrderReservation}.
     *
     * @param productSkuQty is the number of type int to be added to a
     * {@link ProductSku} inventory.
     * @param reservationQty replaces a {@link OrderReservation} qty.
     * @param expire replaces the expire_at property of a {@link ShoppingSession}.
     * @param cookie is a unique string property of {@link ShoppingSession}
     *               that is unique to every device that visits our application. It
     *               is needed to find the {@link OrderReservation} and
     *               {@link ProductSku} associated to the device.
     * @param sku is a unique string for every {@link ProductSku}. It is needed
     *            to find the associated {@link ProductSku} to update
     * @param status is of {@link ReservationStatus} and it always has to be PENDING.
     * */
    @Transactional
    @Modifying
    @Query(value = """
    UPDATE product_sku s
    INNER JOIN order_reservation o ON s.sku_id = o.sku_id
    INNER JOIN shopping_session sh ON o.session_id = sh.session_id
    SET
        s.inventory = (s.inventory - :productSkuQty),
        o.qty = :reservationQty,
        o.reference = :reference,
        o.expire_at = :expire
    WHERE s.sku = :sku AND sh.cookie = :cookie AND o.status = :#{#status.name()}
    """)
    void deductFromProductSkuInventoryAndReplaceReservationQty(
            int productSkuQty,
            int reservationQty,
            String reference,
            LocalDateTime expire,
            String cookie,
            String sku,
            @Param(value = "status") ReservationStatus status
    );

    /**
     * Updates a {@link ProductSku} by adding to its existing inventory and replaces
     * the qty of a {@link OrderReservation}.
     *
     * @param productSkuQty is the number of type int to be added to a
     * {@link ProductSku} inventory.
     * @param reservationQty replaces a {@link OrderReservation} qty.
     * @param expire replaces the expire_at property of a {@link ShoppingSession}.
     * @param cookie is a unique string property of {@link ShoppingSession}
     *               that is unique to every device that visits our application. It
     *               is needed to find the {@code OrderReservation} and
     *               {@link ProductSku} associated to the device.
     * @param sku is a unique string for every {@link ProductSku}. It is needed
     *            to find the associated {@link ProductSku} to update
     * @param status is of {@link ReservationStatus} and it always has to be PENDING.
     * */
    @Transactional
    @Modifying
    @Query(value = """
    UPDATE product_sku s
    INNER JOIN order_reservation o ON s.sku_id = o.sku_id
    INNER JOIN shopping_session sh ON o.session_id = sh.session_id
    SET
        s.inventory = (s.inventory + :productSkuQty),
        o.qty = :reservationQty,
        o.reference = :reference,
        o.expire_at = :expire
    WHERE sh.cookie = :cookie AND s.sku = :sku AND o.status = :#{#status.name()}
    """)
    void addToProductSkuInventoryAndReplaceReservationQty(
            int productSkuQty,
            int reservationQty,
            String reference,
            LocalDateTime expire,
            String cookie,
            String sku,
            @Param(value = "status") ReservationStatus status
    );

    // TODO maybe index expire_at AND status?
    @Query("SELECT * FROM order_reservation o WHERE o.expire_at <= :date AND o.status = :status")
    List<OrderReservation> allPendingExpiredReservations(LocalDateTime date, ReservationStatus status);

    @Query("""
    SELECT
        o.reservation_id AS reservationId,
        o.qty AS qty,
        p.sku AS sku
    FROM order_reservation o
    INNER JOIN product_sku p ON o.sku_id = p.sku_id
    WHERE o.session_id = :id AND o.expire_at > :date AND o.status = :status
    """)
    List<OrderReservationDbMapper> allPendingNoneExpiredReservationsAssociatedToShoppingSession(
            @Param("id") long sessionId,
            LocalDateTime date,
            ReservationStatus status
    );

    /**
     * Returns a {@link PaymentDetailDbMapper} consisting of {@link OrderReservation} and
     * {@link ProductSku}.
     *
     * @param reference a unique string for every {@link OrderReservation} object.
     * @return a {@link List} of {@link PaymentDetailDbMapper}.
     * */
    @Query("""
    SELECT
        o.reservation_id AS reservationId,
        o.qty AS qty,
        p.sku_id AS skuId
    FROM order_reservation o
    INNER JOIN product_sku p ON o.sku_id = p.sku_id
    WHERE o.reference = :reference
    """)
    List<PaymentDetailDbMapper> allReservationsByReference(String reference);

    @Transactional
    @Modifying
    @Query(value = """
    INSERT INTO order_reservation(reference, qty, status, expire_at, sku_id, session_id)
    VALUE (:reference, :qty, :#{#status.name()}, :date, :skuId, :sessionId);
    """)
    void saveOrderReservation(
            String reference,
            int qty,
            ReservationStatus status,
            LocalDateTime date,
            long skuId,
            long sessionId
    );

}
package dev.webserver.payment.repository;

import dev.webserver.payment.entity.OrderDetail;
import dev.webserver.payment.entity.PaymentDetail;
import dev.webserver.payment.projection.OrderPojo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Contains native query
 * */
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    /**
     * Retrieves the order history for a given user principal email.
     *
     * @param principal the email associated to a {@link PaymentDetail}.
     * @return a {@link List} of {@link OrderPojo} representing the order history.
     */
    @Query(nativeQuery = true, value = """
    SELECT
    p.created_at AS time,
    p.currency as currency,
    p.amount as total,
    p.reference_id AS paymentId,
    CONCAT('[',
        GROUP_CONCAT(DISTINCT
            JSON_OBJECT(
                'name', prod.name,
                'key', prod.default_image_key,
                'colour', d.colour
            )
        ),
    ']') AS detail
    FROM order_detail o
    INNER JOIN payment_detail p ON o.payment_detail_id = p.payment_detail_id
    INNER JOIN product_sku s ON o.sku_id = s.sku_id
    INNER JOIN product_detail d ON s.detail_id = d.detail_id
    INNER JOIN product prod ON d.product_id = prod.product_id
    WHERE p.email = :principal
    GROUP BY p.reference_id
    """)
    List<OrderPojo> orderHistoryByPrincipal(String principal);

    // TODO repo test
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
    INSERT INTO order_detail(qty, sku_id, session_id)
    VALUE (:qty, :skuId, :detailId);
    """)
    void saveOrderDetail(int qty, long skuId, long detailId);

}

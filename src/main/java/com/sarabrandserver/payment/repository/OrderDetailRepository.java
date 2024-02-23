package com.sarabrandserver.payment.repository;

import com.sarabrandserver.payment.entity.OrderDetail;
import com.sarabrandserver.payment.entity.PaymentDetail;
import com.sarabrandserver.payment.projection.OrderPojo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
     * @return a list of {@link OrderPojo} representing the order history.
     */
    @Query(nativeQuery = true, value = """
    SELECT
    p.created_at AS time,
    p.currency as currency,
    p.amount as total,
    p.payment_id AS paymentId,
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
    GROUP BY p.payment_id
    """)
    List<OrderPojo> orderHistoryByPrincipal(String principal);

}

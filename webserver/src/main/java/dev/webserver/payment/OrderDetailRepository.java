package dev.webserver.payment;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderDetailRepository extends CrudRepository<OrderDetail, Long> {

    /**
     * Retrieves the order history for a given user principal email.
     *
     * @param principal the email associated to a {@link PaymentDetail}.
     * @return a {@link List} of {@link OrderDetailDbMapper} representing the order history.
     */
    @Query(value = """
    SELECT
    p.created_at AS createdAt,
    p.currency as currency,
    p.amount as amount,
    p.reference_id AS referenceId,
    CONCAT('[',
        GROUP_CONCAT(
            DISTINCT JSON_OBJECT(
                'name', prod.name,
                'image_key', prod.default_image_key,
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
    List<OrderDetailDbMapper> orderHistoryByPrincipal(String principal);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO order_detail(qty, sku_id, payment_detail_id) VALUE (:qty, :skuId, :detailId)")
    void saveOrderDetail(int qty, long skuId, long detailId);

}

ALTER TABLE product
    MODIFY COLUMN description VARCHAR(1000);

ALTER TABLE order_detail
    DROP COLUMN created_at;

ALTER TABLE order_detail
    DROP FOREIGN KEY order_detail_ibfk_1,
    DROP COLUMN address_id;

ALTER TABLE payment_detail
    ADD COLUMN address_id BIGINT NOT NULL;

ALTER TABLE payment_detail
    ADD COLUMN currency VARCHAR(20) NOT NULL;
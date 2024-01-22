ALTER TABLE order_detail
    DROP COLUMN product_sku;

ALTER TABLE order_detail
    MODIFY COLUMN payment_detail_id BIGINT NOT NULL;

ALTER TABLE order_detail
    ADD COLUMN sku_id BIGINT NOT NULL;

ALTER TABLE order_detail
    ADD FOREIGN KEY (sku_id) REFERENCES product_sku (sku_id) ON DELETE RESTRICT;
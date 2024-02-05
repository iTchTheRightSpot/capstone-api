ALTER TABLE order_detail
    DROP COLUMN product_sku;

ALTER TABLE order_detail
    MODIFY COLUMN payment_detail_id BIGINT NOT NULL;

ALTER TABLE order_detail
    ADD COLUMN sku_id BIGINT NOT NULL;

ALTER TABLE order_detail
    ADD FOREIGN KEY (sku_id) REFERENCES product_sku (sku_id) ON DELETE NO ACTION;

ALTER TABLE order_reservation
    DROP COLUMN sku;

ALTER TABLE order_reservation
    ADD COLUMN sku_id BIGINT NOT NULL;

ALTER TABLE order_reservation
    ADD FOREIGN KEY (sku_id) REFERENCES product_sku (sku_id) ON DELETE NO ACTION;

ALTER TABLE cart_item
    DROP COLUMN sku;

ALTER TABLE cart_item
    ADD COLUMN sku_id BIGINT NOT NULL;

ALTER TABLE cart_item
    ADD FOREIGN KEY (sku_id) REFERENCES product_sku (sku_id) ON DELETE NO ACTION;
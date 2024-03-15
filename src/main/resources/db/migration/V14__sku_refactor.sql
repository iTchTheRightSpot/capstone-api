ALTER TABLE order_detail
    DROP COLUMN product_sku;

ALTER TABLE order_detail
    MODIFY COLUMN payment_detail_id BIGINT NOT NULL;

ALTER TABLE order_detail
    ADD COLUMN sku_id BIGINT NOT NULL;

ALTER TABLE order_detail
    ADD CONSTRAINT `order_detail_product_sku_fk` FOREIGN KEY (sku_id) REFERENCES product_sku (sku_id) ON DELETE NO ACTION;

ALTER TABLE order_reservation
    DROP COLUMN sku;

ALTER TABLE order_reservation
    ADD COLUMN sku_id BIGINT NOT NULL;

ALTER TABLE order_reservation
    ADD CONSTRAINT `order_reservation_product_sku_fk` FOREIGN KEY (sku_id) REFERENCES product_sku (sku_id) ON DELETE NO ACTION;

ALTER TABLE cart_item
    DROP COLUMN sku;

ALTER TABLE cart_item
    ADD COLUMN sku_id BIGINT NOT NULL;

ALTER TABLE cart_item
    ADD CONSTRAINT `cart_item_product_sku_fk` FOREIGN KEY (sku_id) REFERENCES product_sku (sku_id) ON DELETE NO ACTION;
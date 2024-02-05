ALTER TABLE product_detail
    DROP FOREIGN KEY product_detail_ibfk_4;
ALTER TABLE product_detail
    ADD CONSTRAINT `product_fk` FOREIGN KEY (product_id) REFERENCES product (product_id) ON DELETE NO ACTION;

ALTER TABLE product
    ADD COLUMN weight FLOAT NOT NULL;

ALTER TABLE product
    ADD COLUMN weight_type VARCHAR(2) DEFAULT 'kg';

ALTER TABLE product_image
    DROP FOREIGN KEY product_image_ibfk_1;
ALTER TABLE product_image
    ADD CONSTRAINT `product_detail_fk` FOREIGN KEY (detail_id) REFERENCES product_detail (detail_id) ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS shipping
(
    shipping_id   BIGINT         NOT NULL UNIQUE AUTO_INCREMENT,
    ngn_price     DECIMAL(20, 2) NOT NULL,
    usd_price     DECIMAL(20, 2) NOT NULL,
    shipping_type ENUM ('INTERNATIONAL', 'LOCAL'),
    PRIMARY KEY (shipping_id)
);
ALTER TABLE product_detail
    DROP FOREIGN KEY product_detail_ibfk_4;
ALTER TABLE product_detail
    ADD CONSTRAINT `product_fk` FOREIGN KEY (product_id)
        REFERENCES product (product_id) ON DELETE NO ACTION;

ALTER TABLE product
    ADD COLUMN weight FLOAT(5, 2) NOT NULL;

ALTER TABLE product
    ADD COLUMN weight_type VARCHAR(2) NOT NULL DEFAULT 'kg';

ALTER TABLE product_image
    DROP FOREIGN KEY product_image_ibfk_1;
ALTER TABLE product_image
    ADD CONSTRAINT `product_detail_fk` FOREIGN KEY (detail_id)
        REFERENCES product_detail (detail_id) ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS ship_setting
(
    ship_id   BIGINT         NOT NULL UNIQUE AUTO_INCREMENT,
    country VARCHAR(57) UNIQUE NOT NULL,
    ngn_price     DECIMAL(20, 2) NOT NULL,
    usd_price     DECIMAL(20, 2) NOT NULL,
    PRIMARY KEY (ship_id)
);

INSERT INTO ship_setting (country, ngn_price, usd_price)
    VALUE ('default', 0.00, 0.00);
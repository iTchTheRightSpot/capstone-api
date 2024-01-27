ALTER TABLE product
    ADD COLUMN weight FLOAT NOT NULL;

ALTER TABLE product
    ADD COLUMN weight_type VARCHAR(2) DEFAULT 'kg';

CREATE TABLE IF NOT EXISTS shipping
(
    shipping_id   BIGINT         NOT NULL UNIQUE AUTO_INCREMENT,
    ngn_price     DECIMAL(20, 2) NOT NULL,
    usd_price     DECIMAL(20, 2) NOT NULL,
    shipping_type ENUM ('INTERNATIONAL', 'LOCAL'),
    PRIMARY KEY (shipping_id)
);

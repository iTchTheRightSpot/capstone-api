ALTER TABLE product MODIFY COLUMN currency varchar(10);

CREATE TABLE IF NOT EXISTS price_currency (
    price_currency_id BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    price             DECIMAL     NOT NULL,
    currency          VARCHAR(10) NOT NULL,
    product_id        BIGINT      NOT NULL,
    PRIMARY KEY (price_currency_id),
    FOREIGN KEY (product_id) references product (product_id)
);
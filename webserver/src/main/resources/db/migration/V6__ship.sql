CREATE TABLE IF NOT EXISTS ship_setting
(
    ship_id   BIGINT         NOT NULL UNIQUE AUTO_INCREMENT,
    country   VARCHAR(57)    NOT NULL UNIQUE,
    ngn_price DECIMAL(20, 2) NOT NULL,
    usd_price DECIMAL(20, 2) NOT NULL,
    PRIMARY KEY (ship_id)
);

INSERT INTO ship_setting (country, ngn_price, usd_price) VALUE ('default', 0.00, 0.00);

CREATE TABLE IF NOT EXISTS tax
(
    tax_id BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    name   VARCHAR(5)  NOT NULL UNIQUE,
    rate   FLOAT(6, 4) NOT NULL,
    PRIMARY KEY (tax_id)
);

INSERT INTO tax (name, rate) VALUE ('vat', 0.00);
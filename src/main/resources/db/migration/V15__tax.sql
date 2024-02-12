CREATE TABLE IF NOT EXISTS tax_setting
(
    tax_id     BIGINT            NOT NULL UNIQUE AUTO_INCREMENT,
    name       VARCHAR(5) UNIQUE NOT NULL,
    rate FLOAT(6, 4)       NOT NULL,
    PRIMARY KEY (tax_id)
);

INSERT INTO tax_setting (name, rate) VALUE ('vat', 0.00);
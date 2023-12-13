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

DROP TABLE IF EXISTS address;

DROP TABLE IF EXISTS country;

ALTER TABLE order_detail
    DROP FOREIGN KEY order_detail_ibfk_2,
    DROP COLUMN payment_detail_id;

DROP TABLE IF EXISTS payment_detail;

CREATE TABLE IF NOT EXISTS payment_detail
(
    payment_detail_id  BIGINT         NOT NULL UNIQUE AUTO_INCREMENT,
    firstname_lastname VARCHAR(255)   NOT NULL,
    email              VARCHAR(255)   NOT NULL,
    phone_number       VARCHAR(255)   NOT NULL,
    payment_id         VARCHAR(255)   NOT NULL UNIQUE,
    currency           VARCHAR(20)    NOT NULL,
    amount             DECIMAL(20, 2) NOT NULL,
    payment_provider   VARCHAR(30)    NOT NULL,
    payment_status     VARCHAR(10)    NOT NULL,
    created_at         DATETIME       NOT NULL,
    PRIMARY KEY (payment_detail_id)
);

ALTER TABLE order_detail
    ADD COLUMN payment_detail_id BIGINT;

CREATE TABLE IF NOT EXISTS address
(
    address_id        BIGINT       NOT NULL UNIQUE,
    unit_number       BIGINT,
    street_number     BIGINT       NOT NULL,
    address_1         VARCHAR(255) NOT NULL,
    address_2         VARCHAR(255),
    city              VARCHAR(255) NOT NULL,
    state_or_province VARCHAR(255) NOT NULL,
    postal_zip_code   VARCHAR(255),
    country           VARCHAR(255) NOT NULL,
    PRIMARY KEY (address_id),
    FOREIGN KEY (address_id) REFERENCES payment_detail (payment_detail_id)
);
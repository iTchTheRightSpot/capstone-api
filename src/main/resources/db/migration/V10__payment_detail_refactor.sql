ALTER TABLE product
    MODIFY COLUMN description VARCHAR(1000);

ALTER TABLE product_sku
    ADD CONSTRAINT validate_inventory_is_always_greater_than_zero CHECK ( inventory >= 0 );

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
    full_name VARCHAR(255) NOT NULL,
    email              VARCHAR(255)   NOT NULL,
    phone VARCHAR(20) NOT NULL,
    reference_id VARCHAR(255)   NOT NULL UNIQUE,
    currency           VARCHAR(20)    NOT NULL,
    amount       DECIMAL(20, 3) NOT NULL,
    payment_provider   VARCHAR(30)    NOT NULL,
    payment_status     VARCHAR(10)    NOT NULL,
    paid_at      VARCHAR(30),
    created_at   DATETIME       NOT NULL,
    client_id BIGINT,
    PRIMARY KEY (payment_detail_id),
    CONSTRAINT `payment_detail_clientz_fk` FOREIGN KEY (client_id)
        REFERENCES clientz (client_id) ON DELETE SET NULL
);

CREATE INDEX IX_payment_detail_email_reference_id ON payment_detail (email, reference_id);

ALTER TABLE order_detail
    ADD COLUMN payment_detail_id BIGINT;

CREATE TABLE IF NOT EXISTS address
(
    address_id        BIGINT       NOT NULL UNIQUE,
    address       VARCHAR(255) NOT NULL,
    city          VARCHAR(100) NOT NULL,
    state         VARCHAR(100) NOT NULL,
    postcode      VARCHAR(10),
    country       VARCHAR(100) NOT NULL,
    delivery_info VARCHAR(1000),
    PRIMARY KEY (address_id),
    CONSTRAINT `payment_detail_address_fk` FOREIGN KEY (address_id)
        REFERENCES payment_detail (payment_detail_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS order_reservation
(
    reservation_id BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    sku            VARCHAR(36) NOT NULL,
    reference  VARCHAR(36) NOT NULL,
    qty            INTEGER     NOT NULL,
    status         VARCHAR(10) NOT NULL,
    expire_at      DATETIME    NOT NULL,
    session_id BIGINT,
    PRIMARY KEY (reservation_id),
    CONSTRAINT `shopping_session_fk` FOREIGN KEY (session_id)
        REFERENCES shopping_session (session_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS payment_authorization
(
    authorization_id   BIGINT       NOT NULL UNIQUE,
    authorization_code VARCHAR(50) NOT NULL,
    bin                VARCHAR(50) NOT NULL,
    card_last_4_digits VARCHAR(5)   NOT NULL,
    exp_month          VARCHAR(2)   NOT NULL,
    exp_year           VARCHAR(6)   NOT NULL,
    channel            VARCHAR(10)   NOT NULL,
    card_type          VARCHAR(20)   NOT NULL,
    bank               VARCHAR(100)   NOT NULL,
    country_code       VARCHAR(10)   NOT NULL,
    brand              VARCHAR(20)      NOT NULL,
    is_reusable        boolean   NOT NULL,
    signature          VARCHAR(50)   NOT NULL,
    PRIMARY KEY (authorization_id),
    CONSTRAINT `payment_detail_payment_authorization_fk` FOREIGN KEY (authorization_id)
        REFERENCES payment_detail (payment_detail_id) ON DELETE RESTRICT
);
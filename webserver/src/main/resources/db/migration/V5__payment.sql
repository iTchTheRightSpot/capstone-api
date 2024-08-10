CREATE TABLE IF NOT EXISTS payment_detail
(
    payment_id       BIGINT                       NOT NULL UNIQUE AUTO_INCREMENT,
    fullname         VARCHAR(255)                 NOT NULL,
    email            VARCHAR(255)                 NOT NULL,
    phone            VARCHAR(20)                  NOT NULL,
    reference_id     VARCHAR(255)                 NOT NULL UNIQUE,
    currency         ENUM ('NGN', 'USD')          NOT NULL,
    amount           DECIMAL(20, 3)               NOT NULL,
    payment_provider VARCHAR(30)                  NOT NULL,
    payment_status   ENUM ('CONFIRMED', 'REFUND') NOT NULL,
    paid_at          VARCHAR(255),
    created_at       TIMESTAMP                    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id          BIGINT,
    PRIMARY KEY (payment_id),
    CONSTRAINT `payment_detail_and_user_FK` FOREIGN KEY (user_id) REFERENCES `user` (user_id) ON DELETE RESTRICT,
    INDEX `payment_detail_email_reference_id_IX` (email, reference_id)
);

CREATE TABLE IF NOT EXISTS order_detail
(
    order_id   BIGINT  NOT NULL UNIQUE AUTO_INCREMENT,
    qty        INTEGER NOT NULL,
    sku_id     BIGINT  NOT NULL,
    payment_id BIGINT  NOT NULL,
    PRIMARY KEY (order_id),
    CONSTRAINT `product_sku_and_order_detail_FK` FOREIGN KEY (sku_id) REFERENCES product_sku (sku_id) ON DELETE RESTRICT,
    CONSTRAINT `payment_detail_and_order_detail_FK` FOREIGN KEY (payment_id) REFERENCES payment_detail (payment_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS payment_authorization
(
    authorization_id   BIGINT       NOT NULL UNIQUE,
    authorization_code VARCHAR(50)  NOT NULL,
    bin                VARCHAR(50)  NOT NULL,
    card_last_4_digits VARCHAR(5)   NOT NULL,
    exp_month          VARCHAR(2)   NOT NULL,
    exp_year           VARCHAR(6)   NOT NULL,
    channel            VARCHAR(10)  NOT NULL,
    card_type          VARCHAR(20)  NOT NULL,
    bank               VARCHAR(100) NOT NULL,
    country_code       VARCHAR(10)  NOT NULL,
    brand              VARCHAR(20)  NOT NULL,
    is_reusable        BOOLEAN      NOT NULL,
    signature          VARCHAR(50)  NOT NULL,
    PRIMARY KEY (authorization_id),
    CONSTRAINT `payment_detail_and_payment_authorization_fk` FOREIGN KEY (authorization_id) REFERENCES payment_detail (payment_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS address
(
    address_id    BIGINT       NOT NULL UNIQUE,
    address       VARCHAR(300) NOT NULL,
    city          VARCHAR(255) NOT NULL,
    state         VARCHAR(100) NOT NULL,
    postcode      VARCHAR(10),
    country       VARCHAR(100) NOT NULL,
    delivery_info VARCHAR(1000),
    PRIMARY KEY (address_id),
    CONSTRAINT `payment_detail_and_address_fk` FOREIGN KEY (address_id) REFERENCES payment_detail (payment_id) ON DELETE RESTRICT
);
CREATE TABLE IF NOT EXISTS client_password_reset_token
(
    reset_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    token    VARCHAR(80) UNIQUE,
    PRIMARY KEY (reset_id)
);

CREATE TABLE IF NOT EXISTS clientz
(
    client_id                BIGINT       NOT NULL UNIQUE AUTO_INCREMENT,
    firstname                VARCHAR(255) NOT NULL,
    lastname                 VARCHAR(255) NOT NULL,
    email                    VARCHAR(255) NOT NULL UNIQUE,
    username                 VARCHAR(255) NOT NULL UNIQUE,
    phone_number             VARCHAR(255) NOT NULL,
    password                 VARCHAR(255) NOT NULL,
    enabled                  BOOLEAN      NOT NULL,
    credentials_none_expired BOOLEAN      NOT NULL,
    account_none_expired     BOOLEAN      NOT NULL,
    account_none_locked      BOOLEAN      NOT NULL,
    reset_id                 BIGINT,
    PRIMARY KEY (client_id),
    FOREIGN KEY (reset_id) REFERENCES client_password_reset_token (reset_id)
);

CREATE TABLE IF NOT EXISTS shopping_session
(
    session_id  BIGINT   NOT NULL UNIQUE AUTO_INCREMENT,
    total_price DECIMAL  NOT NULL,
    created_at  DATETIME NOT NULL,
    modified_at DATETIME,
    client_id   BIGINT,
    PRIMARY KEY (session_id),
    FOREIGN KEY (client_id) REFERENCES clientz (client_id)
);

CREATE TABLE IF NOT EXISTS client_role
(
    client_role_id BIGINT       NOT NULL UNIQUE AUTO_INCREMENT,
    role           VARCHAR(255) NOT NULL,
    client_id      BIGINT,
    PRIMARY KEY (client_role_id),
    FOREIGN KEY (client_id) REFERENCES clientz (client_id)
);

CREATE TABLE IF NOT EXISTS country
(
    country_id   BIGINT       NOT NULL UNIQUE AUTO_INCREMENT,
    country_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (country_id)
);

CREATE TABLE IF NOT EXISTS address
(
    address_id    BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    unit_number   BIGINT,
    street_number BIGINT,
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city          VARCHAR(255),
    region        VARCHAR(255),
    postal_code   VARCHAR(255),
    country_id    BIGINT NOT NULL,
    PRIMARY KEY (address_id),
    FOREIGN KEY (country_id) REFERENCES country (country_id)
);

CREATE TABLE IF NOT EXISTS payment_detail
(
    payment_detail_id BIGINT       NOT NULL UNIQUE AUTO_INCREMENT,
    payment_id        VARCHAR(255) NOT NULL UNIQUE,
    amount            DECIMAL      NOT NULL,
    payment_provider  VARCHAR(20)  NOT NULL,
    payment_status    VARCHAR(20)  NOT NULL,
    created_at        DATETIME     NOT NULL,
    modified_at       DATETIME,
    PRIMARY KEY (payment_detail_id)
);

CREATE TABLE IF NOT EXISTS order_detail
(
    order_detail_id   BIGINT   NOT NULL UNIQUE AUTO_INCREMENT,
    amount            DECIMAL  NOT NULL,
    created_at        DATETIME NOT NULL,
    modified_at       DATETIME,
    payment_detail_id BIGINT,
    address_id        BIGINT,
    PRIMARY KEY (order_detail_id),
    FOREIGN KEY (address_id) REFERENCES address (address_id),
    FOREIGN KEY (payment_detail_id) REFERENCES payment_detail (payment_detail_id)
);

CREATE TABLE IF NOT EXISTS order_item
(
    order_item_id   BIGINT   NOT NULL UNIQUE AUTO_INCREMENT,
    created_at      DATETIME NOT NULL,
    modified_at     DATETIME,
    order_detail_id BIGINT,
    PRIMARY KEY (order_item_id),
    FOREIGN KEY (order_detail_id) REFERENCES order_detail (order_detail_id)
);

CREATE TABLE IF NOT EXISTS product_category
(
    category_id        BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    category_name      VARCHAR(50) NOT NULL UNIQUE,
    created_at         DATETIME    NOT NULL,
    modified_at        DATETIME,
    is_visible         BOOLEAN,
    parent_category_id BIGINT,
    PRIMARY KEY (category_id),
    FOREIGN KEY (parent_category_id) REFERENCES product_category (category_id)
);

CREATE TABLE IF NOT EXISTS product_collection
(
    collection_id BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    collection    VARCHAR(50) NOT NULL UNIQUE,
    created_at    DATETIME    NOT NULL,
    modified_at   DATETIME,
    is_visible    BOOLEAN,
    PRIMARY KEY (collection_id)
);

CREATE TABLE IF NOT EXISTS product
(
    product_id        BIGINT       NOT NULL UNIQUE AUTO_INCREMENT,
    name              VARCHAR(50)  NOT NULL UNIQUE,
    description       VARCHAR(400) NOT NULL,
    default_image_key VARCHAR(50)  NOT NULL,
    price             DECIMAL      NOT NULL,
    currency          VARCHAR(50)  NOT NULL,
    category_id       BIGINT,
    collection_id     BIGINT,
    session_id        BIGINT,
    order_item_id     BIGINT,
    PRIMARY KEY (product_id),
    FOREIGN KEY (category_id) REFERENCES product_category (category_id),
    FOREIGN KEY (collection_id) REFERENCES product_collection (collection_id)
);

CREATE TABLE IF NOT EXISTS product_size
(
    size_id BIGINT       NOT NULL UNIQUE AUTO_INCREMENT,
    size    VARCHAR(100) NOT NULL,
    PRIMARY KEY (size_id)
);

CREATE TABLE IF NOT EXISTS product_colour
(
    colour_id BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    colour    VARCHAR(50) NOT NULL,
    PRIMARY KEY (colour_id)
);

CREATE TABLE IF NOT EXISTS product_inventory
(
    inventory_id BIGINT  NOT NULL UNIQUE AUTO_INCREMENT,
    quantity     INTEGER NOT NULL,
    PRIMARY KEY (inventory_id)
);

CREATE TABLE IF NOT EXISTS product_detail
(
    detail_id    BIGINT       NOT NULL UNIQUE AUTO_INCREMENT,
    sku          VARCHAR(100) NOT NULL UNIQUE,
    is_visible   BOOLEAN,
    created_at   DATETIME     NOT NULL,
    modified_at  DATETIME,
    size_id      BIGINT       NOT NULL,
    inventory_id BIGINT       NOT NULL,
    colour_id    BIGINT       NOT NULL,
    product_id   BIGINT       NOT NULL,
    PRIMARY KEY (detail_id),
    FOREIGN KEY (size_id) REFERENCES product_size (size_id),
    FOREIGN KEY (inventory_id) REFERENCES product_inventory (inventory_id),
    FOREIGN KEY (colour_id) REFERENCES product_colour (colour_id),
    FOREIGN KEY (product_id) REFERENCES product (product_id)
);

CREATE TABLE IF NOT EXISTS product_image
(
    image_id   BIGINT       NOT NULL UNIQUE AUTO_INCREMENT,
    image_key  VARCHAR(50)  NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    detail_id  BIGINT       NOT NULL,
    PRIMARY KEY (image_id),
    FOREIGN KEY (detail_id) REFERENCES product_detail (detail_id)
);
ALTER TABLE clientz
    DROP COLUMN username;
ALTER TABLE product
    ADD uuid varchar(36);
ALTER TABLE product_category
    ADD uuid varchar(36);
ALTER TABLE product_detail
    DROP
        FOREIGN KEY product_detail_ibfk_1,
    DROP
        COLUMN size_id;
ALTER TABLE product_detail
    DROP
        FOREIGN KEY product_detail_ibfk_2,
    DROP
        COLUMN inventory_id;
ALTER TABLE product_detail
    DROP
        FOREIGN KEY product_detail_ibfk_3,
    DROP
        COLUMN colour_id;

ALTER TABLE product_detail
    DROP COLUMN sku;
ALTER TABLE product_detail
    DROP COLUMN modified_at;
ALTER TABLE product_detail
    ADD COLUMN colour varchar(100) not null;

DROP TABLE IF EXISTS product_size;
DROP TABLE IF EXISTS product_colour;
DROP TABLE IF EXISTS product_inventory;

CREATE TABLE product_sku
(
    sku_id    BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    sku       VARCHAR(36) NOT NULL UNIQUE,
    size      VARCHAR(50) NOT NULL,
    inventory INTEGER     NOT NULL,
    detail_id BIGINT      NOT NULL,
    PRIMARY KEY (sku_id),
    FOREIGN KEY (detail_id) references product_detail (detail_id)
);

CREATE INDEX IX_product_sku_sku ON product_sku (sku);

DROP TABLE IF EXISTS SPRING_SESSION_ATTRIBUTES;
DROP TABLE IF EXISTS SPRING_SESSION;
ALTER TABLE clientz
    DROP COLUMN credentials_none_expired;
ALTER TABLE clientz
    DROP COLUMN account_none_expired;
ALTER TABLE clientz
    DROP COLUMN account_none_locked;# product
UPDATE product
SET uuid=(SELECT uuid())
WHERE uuid IS NULL;

ALTER TABLE product
    MODIFY COLUMN uuid varchar(36) NOT NULL UNIQUE;
CREATE INDEX IX_product_uuid ON product (uuid);

# category
UPDATE product_category
SET uuid=(SELECT uuid())
WHERE uuid IS NULL;
ALTER TABLE product_category
    MODIFY COLUMN uuid varchar(36) NOT NULL UNIQUE;
CREATE INDEX IX_product_category_uuid ON product_category (uuid);

# collection
ALTER TABLE product_collection
    ADD uuid varchar(36) NOT NULL UNIQUE;
CREATE INDEX IX_product_collection_uuid ON product_collection (uuid);
ALTER TABLE product
    MODIFY COLUMN currency varchar(10);

CREATE TABLE IF NOT EXISTS price_currency
(
    price_currency_id BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    price             DECIMAL     NOT NULL,
    currency          VARCHAR(10) NOT NULL,
    product_id        BIGINT      NOT NULL,
    PRIMARY KEY (price_currency_id),
    CONSTRAINT `price_currency_product_fk` FOREIGN KEY (product_id) references product (product_id) ON DELETE CASCADE
);# reduce the size of role in client_role table
ALTER TABLE client_role
    CHANGE COLUMN role role VARCHAR(10);

# drop price and currency columns
ALTER TABLE product
    DROP COLUMN price;

ALTER TABLE product
    DROP COLUMN currency;

# CartItem table
CREATE TABLE IF NOT EXISTS cart_item
(
    cart_id    BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    qty        INTEGER     NOT NULL,
    sku        VARCHAR(36) NOT NULL,
    session_id BIGINT      NOT NULL,
    PRIMARY KEY (cart_id),
    FOREIGN KEY (session_id) REFERENCES shopping_session (session_id)
);

# drop relationship between ShoppingSession and product table
ALTER TABLE product
    DROP COLUMN session_id;

# drop and rename columns
ALTER TABLE shopping_session
    ADD COLUMN ip_address VARCHAR(39) NOT NULL UNIQUE;

ALTER TABLE shopping_session
    DROP FOREIGN KEY shopping_session_ibfk_1,
    DROP COLUMN client_id;

ALTER TABLE shopping_session
    DROP COLUMN total_price;

ALTER TABLE shopping_session
    RENAME COLUMN modified_at TO expire_at;# about decimals https://dev.mysql.com/doc/refman/8.0/en/fixed-point-types.html#:~:text=Standard%20SQL%20requires%20that%20DECIMAL,DECIMAL(%20M%20%2C0)%20.

ALTER TABLE payment_detail
    MODIFY COLUMN amount DECIMAL(20, 2);

ALTER TABLE price_currency
    MODIFY COLUMN price DECIMAL(10, 2);

ALTER TABLE payment_detail
    DROP COLUMN modified_at;

# Drop relationship between order_item and product
ALTER TABLE product
    DROP COLUMN order_item_id;

# payment_detail table customization
ALTER TABLE payment_detail
    ADD COLUMN customer_name varchar(255) NOT NULL;
ALTER TABLE payment_detail
    ADD COLUMN email varchar(255) NOT NULL;
ALTER TABLE payment_detail
    ADD COLUMN phone_number varchar(100) NOT NULL;
ALTER TABLE payment_detail
    MODIFY COLUMN payment_status VARCHAR(10);

# order_detail table customization
ALTER TABLE order_detail
    DROP COLUMN modified_at;
ALTER TABLE order_detail
    DROP COLUMN amount;
ALTER TABLE order_detail
    ADD COLUMN product_sku varchar(36) NOT NULL;
ALTER TABLE order_detail
    ADD COLUMN qty INTEGER NOT NULL;

# drop order_item table
DROP TABLE IF EXISTS order_item;
ALTER TABLE shopping_session
    RENAME COLUMN ip_address TO cookie;

ALTER TABLE product
    MODIFY COLUMN description VARCHAR(700);

ALTER TABLE cart_item
    DROP FOREIGN KEY cart_item_ibfk_1;

ALTER TABLE cart_item
    ADD CONSTRAINT cart_item_ibfk_1
        FOREIGN KEY (session_id)
            REFERENCES shopping_session (session_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

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
    payment_detail_id BIGINT         NOT NULL UNIQUE AUTO_INCREMENT,
    full_name         VARCHAR(255)   NOT NULL,
    email             VARCHAR(255)   NOT NULL,
    phone             VARCHAR(20)    NOT NULL,
    reference_id      VARCHAR(255)   NOT NULL UNIQUE,
    currency          VARCHAR(20)    NOT NULL,
    amount            DECIMAL(20, 3) NOT NULL,
    payment_provider  VARCHAR(30)    NOT NULL,
    payment_status    VARCHAR(10)    NOT NULL,
    paid_at           VARCHAR(30),
    created_at        DATETIME       NOT NULL,
    client_id         BIGINT,
    PRIMARY KEY (payment_detail_id),
    CONSTRAINT `payment_detail_clientz_fk` FOREIGN KEY (client_id)
        REFERENCES clientz (client_id) ON DELETE SET NULL
);

CREATE INDEX IX_payment_detail_email_reference_id ON payment_detail (email, reference_id);

ALTER TABLE order_detail
    ADD COLUMN payment_detail_id BIGINT;

CREATE TABLE IF NOT EXISTS address
(
    address_id    BIGINT       NOT NULL UNIQUE,
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
    reference      VARCHAR(36) NOT NULL,
    qty            INTEGER     NOT NULL,
    status         VARCHAR(10) NOT NULL,
    expire_at      DATETIME    NOT NULL,
    session_id     BIGINT,
    PRIMARY KEY (reservation_id),
    CONSTRAINT `shopping_session_fk` FOREIGN KEY (session_id)
        REFERENCES shopping_session (session_id) ON DELETE SET NULL
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
    is_reusable        boolean      NOT NULL,
    signature          VARCHAR(50)  NOT NULL,
    PRIMARY KEY (authorization_id),
    CONSTRAINT `payment_detail_payment_authorization_fk` FOREIGN KEY (authorization_id)
        REFERENCES payment_detail (payment_detail_id) ON DELETE RESTRICT
);
ALTER TABLE product_category
    RENAME COLUMN category_name TO name;

ALTER TABLE product_category
    DROP INDEX IX_product_category_uuid,
    DROP COLUMN uuid,
    DROP COLUMN created_at,
    DROP COLUMN modified_at;

ALTER TABLE product
    DROP FOREIGN KEY product_ibfk_2,
    DROP COLUMN collection_id;

ALTER TABLE product_collection
    DROP INDEX IX_product_collection_uuid;

DROP TABLE product_collection;

ALTER TABLE product_category
    DROP FOREIGN KEY product_category_ibfk_1;
ALTER TABLE product_category
    ADD CONSTRAINT `product_category_fk` FOREIGN KEY (parent_category_id) REFERENCES product_category (category_id) ON DELETE NO ACTION;

ALTER TABLE product
    DROP FOREIGN KEY product_ibfk_1;
ALTER TABLE product
    ADD CONSTRAINT `product_product_category_fk` FOREIGN KEY (category_id) REFERENCES product_category (category_id) ON DELETE NO ACTION;
ALTER TABLE clientz
    DROP FOREIGN KEY clientz_ibfk_1,
    DROP COLUMN reset_id;

DROP TABLE client_password_reset_token;

ALTER TABLE client_role
    RENAME COLUMN client_role_id TO role_id;

ALTER TABLE client_role
    MODIFY COLUMN role ENUM ('CLIENT', 'WORKER');

ALTER TABLE price_currency
    MODIFY COLUMN currency ENUM ('NGN', 'USD');

ALTER TABLE payment_detail
    MODIFY COLUMN currency ENUM ('NGN', 'USD');

ALTER TABLE payment_detail
    MODIFY COLUMN payment_status ENUM ('CONFIRMED', 'REFUND');

ALTER TABLE order_reservation
    MODIFY COLUMN status ENUM ('CONFIRMED', 'PENDING');
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
    ship_id   BIGINT             NOT NULL UNIQUE AUTO_INCREMENT,
    country   VARCHAR(57) UNIQUE NOT NULL,
    ngn_price DECIMAL(20, 2)     NOT NULL,
    usd_price DECIMAL(20, 2)     NOT NULL,
    PRIMARY KEY (ship_id)
);

INSERT INTO ship_setting (country, ngn_price, usd_price)
    VALUE ('default', 0.00, 0.00);
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
CREATE TABLE IF NOT EXISTS tax_setting
(
    tax_id BIGINT            NOT NULL UNIQUE AUTO_INCREMENT,
    name   VARCHAR(5) UNIQUE NOT NULL,
    rate   FLOAT(6, 4)       NOT NULL,
    PRIMARY KEY (tax_id)
);

INSERT INTO tax_setting (name, rate) VALUE ('vat', 0.00);
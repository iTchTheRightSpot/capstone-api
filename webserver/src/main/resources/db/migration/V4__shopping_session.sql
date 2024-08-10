CREATE TABLE IF NOT EXISTS shopping_session
(
    session_id BIGINT       NOT NULL UNIQUE AUTO_INCREMENT,
    cookie     VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_at  TIMESTAMP    NOT NULL,
    PRIMARY KEY (session_id),
    INDEX `shopping_session_expire_at_IX` (expire_at)
);

CREATE TABLE IF NOT EXISTS cart
(
    cart_id    BIGINT  NOT NULL UNIQUE AUTO_INCREMENT,
    qty        INTEGER NOT NULL,
    session_id BIGINT  NOT NULL,
    sku_id     BIGINT  NOT NULL,
    PRIMARY KEY (cart_id),
    CONSTRAINT `shopping_session_cart_FK` FOREIGN KEY (session_id) REFERENCES shopping_session (session_id) ON DELETE CASCADE,
    CONSTRAINT `product_sku_cart_FK` FOREIGN KEY (sku_id) REFERENCES product_sku (sku_id) ON DELETE CASCADE,
    CONSTRAINT `cart_qty_greater_than_zero_CHECK` CHECK ( qty >= 0 )
);

CREATE TABLE IF NOT EXISTS order_reservation
(
    reservation_id BIGINT                        NOT NULL UNIQUE AUTO_INCREMENT,
    reference      VARCHAR(36)                   NOT NULL UNIQUE,
    qty            INTEGER                       NOT NULL,
    status         ENUM ('CONFIRMED', 'PENDING') NOT NULL DEFAULT 'PENDING',
    expire_at      TIMESTAMP                     NOT NULL,
    session_id     BIGINT,
    sku_id         BIGINT,
    PRIMARY KEY (reservation_id),
    CONSTRAINT `shopping_session_and_order_reservation_fk` FOREIGN KEY (session_id) REFERENCES shopping_session (session_id) ON DELETE SET NULL,
    CONSTRAINT `product_sku_and_order_reservation_fk` FOREIGN KEY (session_id) REFERENCES product_sku (session_id) ON DELETE SET NULL
);
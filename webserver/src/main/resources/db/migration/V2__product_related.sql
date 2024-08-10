CREATE TABLE IF NOT EXISTS product
(
    product_id        BIGINT        NOT NULL UNIQUE AUTO_INCREMENT,
    uuid              VARCHAR(36)   NOT NULL UNIQUE,
    name              VARCHAR(50)   NOT NULL UNIQUE,
    description       VARCHAR(2000) NOT NULL,
    default_image_key VARCHAR(36)   NOT NULL UNIQUE,
    weight            FLOAT(5, 2)   NOT NULL DEFAULT 0.0,
    weight_type       VARCHAR(2)    NOT NULL DEFAULT 'kg',
    category_id       BIGINT        NOT NULL,
    PRIMARY KEY (product_id),
    CONSTRAINT `product_category_and_product_FK` FOREIGN KEY (category_id) references category (category_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS product_price_currency
(
    currency_id BIGINT              NOT NULL UNIQUE AUTO_INCREMENT,
    price       DECIMAL(10, 2)      NOT NULL,
    currency    ENUM ('NGN', 'USD') NOT NULL DEFAULT 'NGN',
    product_id  BIGINT              NOT NULL,
    PRIMARY KEY (currency_id),
    CONSTRAINT `product_and_price_currency_FK` FOREIGN KEY (product_id) references product (product_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_detail
(
    detail_id  BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    colour     VARCHAR(50) NOT NULL,
    is_visible BOOLEAN              DEFAULT FALSE,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    product_id BIGINT      NOT NULL,
    PRIMARY KEY (detail_id),
    CONSTRAINT `product_and_product_detail_FK` FOREIGN KEY (product_id) references product (product_id) ON DELETE RESTRICT
);

CREATE TABLE product_sku
(
    sku_id    BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    sku       VARCHAR(36) NOT NULL UNIQUE,
    size      VARCHAR(50) NOT NULL,
    inventory INTEGER     NOT NULL,
    detail_id BIGINT      NOT NULL,
    PRIMARY KEY (sku_id),
    CONSTRAINT `product_detail_and_product_sku_FK` FOREIGN KEY (detail_id) references product_detail (detail_id) ON DELETE RESTRICT,
    CONSTRAINT `product_sku_inventory_greater_than_zero_CHECK` CHECK ( inventory >= 0 )
);

CREATE TABLE product_image
(
    image_id  BIGINT      NOT NULL UNIQUE AUTO_INCREMENT,
    image_key VARCHAR(36) NOT NULL UNIQUE,
    detail_id BIGINT      NOT NULL,
    PRIMARY KEY (image_id),
    CONSTRAINT `product_detail_and_product_image_FK` FOREIGN KEY (detail_id) references product_detail (detail_id) ON DELETE CASCADE
);
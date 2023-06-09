CREATE TABLE IF NOT EXISTS product_category (
    category_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    category_name VARCHAR(255) NOT NULL UNIQUE,
    parent_category_id BIGINT,
    PRIMARY KEY (category_id),
    FOREIGN KEY (parent_category_id) REFERENCES product_category (category_id)
);

CREATE TABLE IF NOT EXISTS product (
    product_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    image_path VARCHAR(255) NOT NULL,
    category_id BIGINT,
    PRIMARY KEY (product_id),
    FOREIGN KEY (category_id) REFERENCES product_category (category_id)
);

CREATE TABLE IF NOT EXISTS product_item (
    product_item_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    sku VARCHAR(255) NOT NULL UNIQUE,
    qty_in_stock INTEGER NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    price DECIMAL NOT NULL,
    product_id BIGINT,
    PRIMARY KEY (product_item_id),
    FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE IF NOT EXISTS variation (
    variation_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category_id BIGINT,
    PRIMARY KEY (variation_id),
    FOREIGN KEY (category_id) REFERENCES product_category (category_id)
);

CREATE TABLE IF NOT EXISTS variation_option (
    variation_option_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    value INTEGER NOT NULL,
    variation_id BIGINT,
    PRIMARY KEY (variation_option_id),
    FOREIGN KEY (variation_id) REFERENCES variation(variation_id)
);

CREATE TABLE IF NOT EXISTS product_configuration (
    product_configuration_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    product_item_id BIGINT,
    variation_option_id BIGINT,
    PRIMARY KEY (product_configuration_id),
    FOREIGN KEY (product_item_id) REFERENCES product_item (product_item_id),
    FOREIGN KEY (variation_option_id) REFERENCES variation_option (variation_option_id)
);

CREATE TABLE IF NOT EXISTS promotion (
    promotion_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    discount_rate DECIMAL NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    PRIMARY KEY (promotion_id)
);

CREATE TABLE IF NOT EXISTS promotion_category (
    promotion_category_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    category_id BIGINT,
    promotion_id BIGINT,
    PRIMARY KEY (promotion_category_id),
    FOREIGN KEY (category_id) REFERENCES product_category(category_id),
    FOREIGN KEY (promotion_id) REFERENCES promotion(promotion_id)
);
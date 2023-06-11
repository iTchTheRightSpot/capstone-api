CREATE TABLE IF NOT EXISTS worker_password_reset_token (
    reset_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    token VARCHAR(80) UNIQUE,
    PRIMARY KEY (reset_id)
);

CREATE TABLE IF NOT EXISTS worker (
    worker_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL,
    credentials_none_expired BOOLEAN NOT NULL,
    account_none_expired BOOLEAN NOT NULL,
    locked BOOLEAN NOT NULL,
    reset_id BIGINT,
    PRIMARY KEY (worker_id),
    FOREIGN KEY (reset_id) REFERENCES worker_password_reset_token (reset_id)
);

CREATE TABLE IF NOT EXISTS worker_role (
    worker_role_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    role VARCHAR(255) NOT NULL,
    worker_id BIGINT,
    PRIMARY KEY (worker_role_id),
    FOREIGN KEY (worker_id) REFERENCES worker (worker_id)
);

CREATE TABLE IF NOT EXISTS client_password_reset_token (
    reset_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    token VARCHAR(80) UNIQUE,
    PRIMARY KEY (reset_id)
);

CREATE TABLE IF NOT EXISTS clientz (
    client_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL,
    credentials_none_expired BOOLEAN NOT NULL,
    account_none_expired BOOLEAN NOT NULL,
    locked BOOLEAN NOT NULL,
    reset_id BIGINT,
    PRIMARY KEY (client_id),
    FOREIGN KEY (reset_id) REFERENCES client_password_reset_token (reset_id)
);

CREATE TABLE IF NOT EXISTS shopping_session (
    session_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    total_price DECIMAL NOT NULL,
    created_at DATETIME NOT NULL,
    modified_at DATETIME,
    client_id BIGINT,
    PRIMARY KEY (session_id),
    FOREIGN KEY (client_id) REFERENCES clientz(client_id)
);

CREATE TABLE IF NOT EXISTS client_role (
    client_role_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    role VARCHAR(255) NOT NULL,
    client_id BIGINT,
    PRIMARY KEY (client_role_id),
    FOREIGN KEY (client_id) REFERENCES clientz (client_id)
);

CREATE TABLE IF NOT EXISTS country (
    country_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    country_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (country_id)
);

CREATE TABLE IF NOT EXISTS address (
    address_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    unit_number BIGINT,
    street_number BIGINT,
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(255),
    region VARCHAR(255),
    postal_code VARCHAR(255),
    country_id BIGINT NOT NULL,
    PRIMARY KEY (address_id),
    FOREIGN KEY (country_id) REFERENCES country (country_id)
);

CREATE TABLE IF NOT EXISTS payment_detail (
    payment_detail_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    payment_id VARCHAR(255) NOT NULL UNIQUE,
    amount DECIMAL NOT NULL,
    payment_provider VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    modified_at DATETIME,
    PRIMARY KEY (payment_detail_id)
);

CREATE TABLE IF NOT EXISTS order_detail (
    order_detail_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    amount DECIMAL NOT NULL,
    created_at DATETIME NOT NULL,
    modified_at DATETIME,
    payment_detail_id BIGINT,
    address_id BIGINT,
    PRIMARY KEY (order_detail_id),
    FOREIGN KEY (address_id) REFERENCES address(address_id),
    FOREIGN KEY (payment_detail_id) REFERENCES payment_detail(payment_detail_id)
);

CREATE TABLE IF NOT EXISTS order_item (
    order_item_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    created_at DATETIME NOT NULL,
    modified_at DATETIME,
    order_detail_id BIGINT,
    PRIMARY KEY (order_item_id),
    FOREIGN KEY (order_detail_id) REFERENCES order_detail(order_detail_id)
);

CREATE TABLE IF NOT EXISTS product_category (
    category_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    modified_at DATETIME,
    deleted_at DATETIME,
    parent_category_id BIGINT,
    PRIMARY KEY (category_id),
    FOREIGN KEY (parent_category_id) REFERENCES product_category (category_id)
);

CREATE TABLE IF NOT EXISTS product_collection (
    product_collection_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    collection VARCHAR(32) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    modified_at DATETIME,
    deleted_at DATETIME,
    PRIMARY KEY (product_collection_id)
);

CREATE TABLE IF NOT EXISTS product (
    product_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    default_image_path VARCHAR(255) NOT NULL,
    category_id BIGINT,
    product_collection_id BIGINT,
    session_id BIGINT,
    order_item_id BIGINT,
    PRIMARY KEY (product_id),
    FOREIGN KEY (category_id) REFERENCES product_category (category_id),
    FOREIGN KEY (product_collection_id) REFERENCES product_collection (product_collection_id)
);

CREATE TABLE IF NOT EXISTS product_detail (
    product_detail_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    description VARCHAR(255),
    sku VARCHAR(255) NOT NULL UNIQUE,
    qty INTEGER NOT NULL,
    created_at DATETIME NOT NULL,
    modified_at DATETIME,
    deleted_at DATETIME,
    product_id BIGINT,
    PRIMARY KEY (product_detail_id),
    FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE IF NOT EXISTS product_image (
    product_image_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    image_path VARCHAR(255) NOT NULL,
    product_detail_id BIGINT,
    PRIMARY KEY (product_image_id),
    FOREIGN KEY (product_detail_id) REFERENCES product_detail(product_detail_id)
);

CREATE TABLE IF NOT EXISTS product_size (
    product_size_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    size VARCHAR(255) NOT NULL,
    product_detail_id BIGINT,
    PRIMARY KEY (product_size_id),
    FOREIGN KEY (product_detail_id) REFERENCES product_detail(product_detail_id)
);

CREATE TABLE IF NOT EXISTS product_colour (
    product_colour_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    colour VARCHAR(20) NOT NULL,
    product_detail_id BIGINT,
    PRIMARY KEY (product_colour_id),
    FOREIGN KEY (product_detail_id) REFERENCES product_detail(product_detail_id)
);

CREATE TABLE IF NOT EXISTS product_price (
    product_price_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    price DECIMAL NOT NULL,
    product_detail_id BIGINT,
    PRIMARY KEY (product_price_id),
    FOREIGN KEY (product_detail_id) REFERENCES product_detail(product_detail_id)
);

CREATE TABLE IF NOT EXISTS currency_entity (
    currency_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    currency VARCHAR(25) NOT NULL,
    product_price_id BIGINT,
    PRIMARY KEY (currency_id),
    FOREIGN KEY (product_price_id) REFERENCES product_price(product_price_id)
);
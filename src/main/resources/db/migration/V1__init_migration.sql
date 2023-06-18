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
    username VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL,
    credentials_none_expired BOOLEAN NOT NULL,
    account_none_expired BOOLEAN NOT NULL,
    account_none_locked BOOLEAN NOT NULL,
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
    is_visible BOOLEAN,
    parent_category_id BIGINT,
    PRIMARY KEY (category_id),
    FOREIGN KEY (parent_category_id) REFERENCES product_category (category_id)
);

CREATE TABLE IF NOT EXISTS product_collection (
    collection_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    collection VARCHAR(50) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    modified_at DATETIME,
    is_visible BOOLEAN,
    PRIMARY KEY (collection_id)
);

CREATE TABLE IF NOT EXISTS product (
    product_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL UNIQUE,
    default_image_path VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    price DECIMAL NOT NULL,
    currency VARCHAR(50) NOT NULL,
    category_id BIGINT,
    collection_id BIGINT,
    session_id BIGINT,
    order_item_id BIGINT,
    PRIMARY KEY (product_id),
    FOREIGN KEY (category_id) REFERENCES product_category (category_id),
    FOREIGN KEY (collection_id) REFERENCES product_collection (collection_id)
);

CREATE TABLE IF NOT EXISTS product_image (
    image_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    image_key VARCHAR(50) NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    PRIMARY KEY (image_id)
);

CREATE TABLE IF NOT EXISTS product_size (
    size_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    size VARCHAR(100) NOT NULL,
    PRIMARY KEY (size_id)
);

CREATE TABLE IF NOT EXISTS product_colour (
    colour_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    colour VARCHAR(50) NOT NULL,
    PRIMARY KEY (colour_id)
);

CREATE TABLE IF NOT EXISTS product_inventory (
    inventory_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    quantity INTEGER NOT NULL,
    PRIMARY KEY (inventory_id)
);

CREATE TABLE IF NOT EXISTS product_detail (
    detail_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    sku VARCHAR(100) NOT NULL UNIQUE,
    is_visible BOOLEAN,
    created_at DATETIME NOT NULL,
    modified_at DATETIME,
    size_id BIGINT NOT NULL,
    inventory_id BIGINT NOT NULL,
    image_id BIGINT NOT NULL,
    colour_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (detail_id),
    FOREIGN KEY (size_id) REFERENCES product_size (size_id),
    FOREIGN KEY (inventory_id) REFERENCES product_inventory (inventory_id),
    FOREIGN KEY (image_id) REFERENCES product_image (image_id),
    FOREIGN KEY (colour_id) REFERENCES product_colour (colour_id),
    FOREIGN KEY (product_id) REFERENCES product (product_id)
);
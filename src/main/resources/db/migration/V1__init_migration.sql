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
    PRIMARY KEY (worker_id)
);

CREATE TABLE IF NOT EXISTS worker_role (
    worker_role_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    role VARCHAR(255) NOT NULL,
    worker_id BIGINT,
    PRIMARY KEY (worker_role_id),
    FOREIGN KEY (worker_id) REFERENCES worker (worker_id)
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
    PRIMARY KEY (client_id)
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

CREATE TABLE IF NOT EXISTS client_address (
    client_address_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    is_default BOOLEAN NOT NULL,
    client_id BIGINT,
    address_id BIGINT,
    PRIMARY KEY (client_address_id),
    FOREIGN KEY (client_id) REFERENCES clientz (client_id),
    FOREIGN KEY (address_id) REFERENCES address (address_id)
);
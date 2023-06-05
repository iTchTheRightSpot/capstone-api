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
    role ENUM('WORKER') NOT NULL,
    worker_id BIGINT,
    PRIMARY KEY (worker_role_id),
    FOREIGN KEY (worker_id) REFERENCES worker (worker_id)
);
CREATE TABLE IF NOT EXISTS clientz (
    client_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL,
    credentials_none_expired BOOLEAN NOT NULL,
    account_none_expired BOOLEAN NOT NULL,
    locked BOOLEAN NOT NULL,
    PRIMARY KEY (client_id)
);
CREATE TABLE IF NOT EXISTS client_role (
    client_role_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    role ENUM('CLIENT') NOT NULL,
    client_id BIGINT,
    PRIMARY KEY (client_role_id),
    FOREIGN KEY (client_id) REFERENCES clientz (client_id)
);
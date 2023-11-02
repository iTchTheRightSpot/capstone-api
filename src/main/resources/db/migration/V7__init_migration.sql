# reduce the size of role in client_role table
ALTER TABLE client_role CHANGE COLUMN role role VARCHAR(10);

# drop price and currency columns
ALTER TABLE product
    DROP COLUMN price;

ALTER TABLE product
    DROP COLUMN currency;

# CartItem table
CREATE TABLE IF NOT EXISTS cart_item
(
    cart_id    BIGINT  NOT NULL UNIQUE AUTO_INCREMENT,
    qty        INTEGER NOT NULL,
    sku VARCHAR(36) NOT NULL,
    session_id BIGINT  NOT NULL,
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
    RENAME COLUMN modified_at TO expire_at;
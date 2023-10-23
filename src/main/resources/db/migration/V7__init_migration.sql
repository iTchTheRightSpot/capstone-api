# rename product columns
ALTER TABLE product
    RENAME COLUMN price TO default_price;
ALTER TABLE product
    RENAME COLUMN currency TO default_currency;

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
    DROP COLUMN total_price;
ALTER TABLE shopping_session
    RENAME COLUMN modified_at TO expire_at;
ALTER TABLE product RENAME COLUMN price TO default_price;
ALTER TABLE product RENAME COLUMN currency TO default_currency;

# drop relationship between ShoppingSession and product table
ALTER TABLE product
    DROP COLUMN session_id;

# create 1 to many relationship between ShoppingSession and ProductSKU
ALTER TABLE product_sku
    ADD COLUMN session_id BIGINT;
ALTER TABLE product_sku
    ADD FOREIGN KEY (session_id) references shopping_session (session_id);

# drop and rename columns
ALTER TABLE shopping_session
    DROP COLUMN total_price;
ALTER TABLE shopping_session
    ADD COLUMN qty INTEGER NOT NULL;
ALTER TABLE shopping_session RENAME COLUMN modified_at TO expire_at;
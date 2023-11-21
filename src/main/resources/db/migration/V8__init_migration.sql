# about decimals https://dev.mysql.com/doc/refman/8.0/en/fixed-point-types.html#:~:text=Standard%20SQL%20requires%20that%20DECIMAL,DECIMAL(%20M%20%2C0)%20.

ALTER TABLE payment_detail
    MODIFY COLUMN amount DECIMAL(20, 2);

ALTER TABLE price_currency
    MODIFY COLUMN price DECIMAL(10, 2);

ALTER TABLE payment_detail
    DROP COLUMN modified_at;

# Drop relationship between order_item and product
ALTER TABLE product
    DROP COLUMN order_item_id;

# payment_detail table customization
ALTER TABLE payment_detail
    ADD COLUMN customer_name varchar(255) NOT NULL;
ALTER TABLE payment_detail
    ADD COLUMN email varchar(255) NOT NULL;
ALTER TABLE payment_detail
    ADD COLUMN phone_number varchar(100) NOT NULL;
ALTER TABLE payment_detail
    MODIFY COLUMN payment_status VARCHAR(10);

# order_detail table customization
ALTER TABLE order_detail
    DROP COLUMN modified_at;
ALTER TABLE order_detail
    DROP COLUMN amount;
ALTER TABLE order_detail
    ADD COLUMN product_sku varchar(36) NOT NULL;
ALTER TABLE order_detail
    ADD COLUMN qty INTEGER NOT NULL;

# drop order_item table
DROP TABLE IF EXISTS order_item;
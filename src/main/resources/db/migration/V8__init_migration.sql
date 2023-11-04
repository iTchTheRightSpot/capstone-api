# about decimals https://dev.mysql.com/doc/refman/8.0/en/fixed-point-types.html#:~:text=Standard%20SQL%20requires%20that%20DECIMAL,DECIMAL(%20M%20%2C0)%20.

ALTER TABLE payment_detail
    MODIFY COLUMN amount DECIMAL(20, 2);

ALTER TABLE price_currency
    MODIFY COLUMN price DECIMAL(10, 2);
ALTER TABLE clientz
    DROP FOREIGN KEY clientz_ibfk_1,
    DROP COLUMN reset_id;

DROP TABLE client_password_reset_token;

ALTER TABLE client_role
    RENAME COLUMN client_role_id TO role_id;

ALTER TABLE client_role
    MODIFY COLUMN role ENUM ('CLIENT', 'WORKER');

ALTER TABLE price_currency
    MODIFY COLUMN currency ENUM ('NGN', 'USD');

ALTER TABLE payment_detail
    MODIFY COLUMN currency ENUM ('NGN', 'USD');

ALTER TABLE payment_detail
    MODIFY COLUMN payment_status ENUM ('CONFIRMED', 'REFUND');

ALTER TABLE order_reservation
    MODIFY COLUMN status ENUM ('CONFIRMED', 'PENDING');
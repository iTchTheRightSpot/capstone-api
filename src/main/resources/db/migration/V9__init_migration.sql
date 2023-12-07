ALTER TABLE shopping_session
    RENAME COLUMN ip_address TO cookie;

ALTER TABLE product
    MODIFY COLUMN description VARCHAR(700);

ALTER TABLE cart_item
    DROP FOREIGN KEY cart_item_ibfk_1;

ALTER TABLE cart_item
    ADD CONSTRAINT cart_item_ibfk_1
        FOREIGN KEY (session_id)
            REFERENCES shopping_session (session_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;
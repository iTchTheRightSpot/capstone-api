ALTER TABLE shopping_session
    RENAME COLUMN ip_address TO cookie;

ALTER TABLE product
    MODIFY COLUMN description VARCHAR(700);
# About dropping foreign keys. NOTE ibfk_1 is needed. The num digit is the point of creating of
# constraint in migration script
# https://dev.mysql.com/doc/refman/8.0/en/create-table-foreign-keys.html#:~:text=You%20can%20drop%20a%20foreign,drop%20the%20foreign%20key%20constraint.
ALTER TABLE product_detail
    DROP FOREIGN KEY product_detail_ibfk_1,
    DROP COLUMN size_id;
ALTER TABLE product_detail
    DROP FOREIGN KEY product_detail_ibfk_2,
    DROP COLUMN inventory_id;
ALTER TABLE product_detail
    DROP FOREIGN KEY product_detail_ibfk_3,
    DROP COLUMN colour_id;

ALTER TABLE product_detail ADD COLUMN colour varchar(100) not null;

DROP TABLE IF EXISTS product_size;
DROP TABLE IF EXISTS product_colour;
DROP TABLE IF EXISTS product_inventory;

CREATE TABLE product_size_inventory (
    size_inventory_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    size VARCHAR(100) NOT NULL,
    inventory INTEGER NOT NULL,
    PRIMARY KEY (size_inventory_id)
);

ALTER TABLE product_detail
    ADD COLUMN size_inventory_id BIGINT NOT NULL,
    ADD CONSTRAINT FOREIGN KEY(size_inventory_id) REFERENCES product_size_inventory(size_inventory_id);
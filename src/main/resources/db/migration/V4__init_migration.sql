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

ALTER TABLE product_detail DROP COLUMN sku;
ALTER TABLE product_detail DROP COLUMN modified_at;
ALTER TABLE product_detail ADD COLUMN colour varchar(100) not null;

DROP TABLE IF EXISTS product_size;
DROP TABLE IF EXISTS product_colour;
DROP TABLE IF EXISTS product_inventory;

CREATE TABLE product_sku (
    sku_id BIGINT NOT NULL UNIQUE AUTO_INCREMENT,
    sku VARCHAR(36) NOT NULL UNIQUE,
    size VARCHAR(50) NOT NULL,
    inventory INTEGER NOT NULL,
    detail_id BIGINT NOT NULL,
    PRIMARY KEY (sku_id),
    FOREIGN KEY (detail_id) references product_detail (detail_id)
);

CREATE INDEX IX_product_sku_sku ON product_sku (sku);

# Migrate to JWT
DROP TABLE IF EXISTS SPRING_SESSION_ATTRIBUTES;
DROP TABLE IF EXISTS SPRING_SESSION;
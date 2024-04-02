ALTER TABLE product_category
    RENAME COLUMN category_name TO name;

ALTER TABLE product_category
    DROP INDEX IX_product_category_uuid,
    DROP COLUMN uuid,
    DROP COLUMN created_at,
    DROP COLUMN modified_at;

ALTER TABLE product
    DROP FOREIGN KEY product_ibfk_2,
    DROP COLUMN collection_id;

ALTER TABLE product_collection
    DROP INDEX IX_product_collection_uuid;

DROP TABLE product_collection;

ALTER TABLE product_category
    DROP FOREIGN KEY product_category_ibfk_1;
ALTER TABLE product_category
    ADD CONSTRAINT `product_category_fk` FOREIGN KEY (parent_category_id) REFERENCES product_category (category_id) ON DELETE RESTRICT;

ALTER TABLE product
    DROP FOREIGN KEY product_ibfk_1;
ALTER TABLE product
    ADD CONSTRAINT `product_product_category_fk` FOREIGN KEY (category_id) REFERENCES product_category (category_id) ON DELETE RESTRICT;
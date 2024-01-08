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
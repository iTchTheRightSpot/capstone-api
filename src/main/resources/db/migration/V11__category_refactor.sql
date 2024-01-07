ALTER TABLE product_category
    RENAME COLUMN category_name TO name;

ALTER TABLE product_category
    DROP INDEX IX_product_category_uuid,
    DROP COLUMN uuid,
    DROP COLUMN created_at,
    DROP COLUMN modified_at;
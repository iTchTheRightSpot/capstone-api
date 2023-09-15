# Migration script updates uuid column for various
# product, product_category and product_collection tables.
# Also creating indexes for these columns
# https://www.w3schools.com/sql/sql_create_index.asp

# product
UPDATE product SET uuid=(SELECT uuid()) WHERE uuid IS NULL;
ALTER TABLE product MODIFY COLUMN uuid varchar(36) NOT NULL UNIQUE;
CREATE INDEX IX_product_uuid ON product (uuid);

# category
UPDATE product_category SET uuid=(SELECT uuid()) WHERE uuid IS NULL;
ALTER TABLE product_category MODIFY COLUMN uuid varchar(36) NOT NULL UNIQUE;
CREATE INDEX IX_product_category_uuid ON product_category (uuid);

# collection
ALTER TABLE product_collection ADD uuid varchar(36) NOT NULL UNIQUE;
CREATE INDEX IX_product_collection_uuid ON product_collection (uuid);
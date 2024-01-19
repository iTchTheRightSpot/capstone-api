ALTER TABLE product
    ADD COLUMN weight FLOAT(7, 5) NOT NULL;

ALTER TABLE product
    ADD COLUMN weight_type VARCHAR(2) DEFAULT 'kg';
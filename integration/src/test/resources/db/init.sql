INSERT IGNORE INTO product_category(category_id, name, is_visible, parent_category_id)
VALUES (1, 'clothes', true, null), (2, 't-shirt', true, 1);

INSERT IGNORE INTO product(product_id, uuid, name, description, default_image_key, weight, weight_type, category_id)
VALUES (1, 'product-uuid', 'product-1', 'lorem 5000', 'image-key', 2.5, 'kg', 1),
       (2, 'product-uuid-2', 'product-2', 'lorem 5000', 'image-key-2', 3.5, 'kg', 1);

INSERT IGNORE INTO price_currency(price_currency_id, price, currency, product_id) VALUE (1, 99.99, 'NGN', 1);

INSERT IGNORE INTO product_detail(detail_id, colour, is_visible, created_at, product_id)
VALUES (1, 'red', true, timestamp('2024-03-26', '12:20:45'), 1),
       (2, 'greenish', true, timestamp('2024-03-26', '12:20:45'), 1);

INSERT IGNORE INTO product_image(image_id, image_key, image_path, detail_id)
    VALUE (1, 'image-key', 'image-path-is', 1);

INSERT IGNORE INTO product_sku(sku_id, sku, size, inventory, detail_id)
VALUES (1, 'product-sku-sku', 'medium', 10, 1),
       (2, 'product-sku-sku-2', 'large', 5, 1);
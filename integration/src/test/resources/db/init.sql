INSERT IGNORE INTO product_category(name, is_visible, parent_category_id)
VALUES ('clothes', true, null), ('t-shirt', true, 1);

INSERT IGNORE INTO product(name, description, default_image_key, category_id, uuid, weight, weight_type)
VALUES ('product-1', 'lorem 5000', 'image-key-1', 1, 'product-uuid-1', 2.5, 'kg'),
       ('product-2', 'lorem 5000', 'image-key-2', 1, 'product-uuid-2', 3.5, 'kg');

INSERT IGNORE INTO price_currency(price, currency, product_id) VALUE (99.99, 'NGN', 1);

INSERT IGNORE INTO product_detail(detail_id, colour, is_visible, created_at, product_id)
VALUES (1, 'red', true, timestamp('2024-03-26', '12:20:45'), 1),
       (2, 'greenish', true, timestamp('2024-03-26', '12:20:45'), 1),
       (3, 'yellow-brown', true, timestamp('2024-03-26', '12:20:45'), 1);;

INSERT IGNORE INTO product_image(image_key, image_path, detail_id) VALUE ('image-key', 'image-path-is', 1);

INSERT IGNORE INTO product_sku(sku, size, inventory, detail_id)
VALUES ('product-sku-1', 'medium', 10, 1),
       ('product-sku-2', 'large', 5, 1),
       ('product-sku-3', 'large', 2, 3);

INSERT IGNORE INTO ship_setting(country, ngn_price, usd_price)
VALUES ('nigeria', 15750, 10.59),
       ('canada', 55000, 50.59);